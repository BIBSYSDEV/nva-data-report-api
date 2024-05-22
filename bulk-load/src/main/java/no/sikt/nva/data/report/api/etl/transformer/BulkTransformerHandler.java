package no.sikt.nva.data.report.api.etl.transformer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.sikt.nva.data.report.api.etl.transformer.util.GzipUtil.compress;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.JsonNode;
import commons.db.utils.DocumentUnwrapper;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.sikt.nva.data.report.api.etl.aws.AwsSqsClient;
import no.sikt.nva.data.report.api.etl.queue.QueueClient;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class BulkTransformerHandler implements RequestHandler<SQSEvent, Void> {

    private static final Logger logger = LoggerFactory.getLogger(BulkTransformerHandler.class);
    private static final Environment ENVIRONMENT = new Environment();
    public static final String API_HOST = ENVIRONMENT.readEnv("API_HOST");
    private static final String LOADER_BUCKET = "LOADER_BUCKET";
    private static final String EXPANDED_RESOURCES_BUCKET = "EXPANDED_RESOURCES_BUCKET";
    private static final String NQUADS_GZIPPED = ".nquads.gz";
    private static final String KEY_BATCHES_BUCKET = ENVIRONMENT.readEnv("KEY_BATCHES_BUCKET");
    private static final String PROCESSING_BATCH_MESSAGE = "Processing batch: {}";
    private static final String LAST_CONSUMED_BATCH = "Last consumed batch: {}";
    private static final String LINE_BREAK = "\n";
    private static final String ID_POINTER = "/id";
    private static final String NT_EXTENSION = ".nt";
    private static final String MISSING_ID_NODE_IN_CONTENT_ERROR = "Missing id-node in content: {}";
    private static final String REGION = "AWS_REGION_NAME";
    private static final String QUEUE_URL = "BULK_TRANSFORMER_QUEUE_URL";
    private final S3Client s3ResourcesClient;
    private final S3Client s3BatchesClient;
    private final S3Client s3OutputClient;
    private final QueueClient queueClient;

    @JacocoGenerated
    public BulkTransformerHandler() {
        this(defaultS3Client(), defaultS3Client(), defaultS3Client(), defaultSqsClient(new Environment()));
    }

    public BulkTransformerHandler(S3Client s3ResourcesClient,
                                  S3Client s3BatchesClient,
                                  S3Client s3OutputClient,
                                  QueueClient queueClient) {
        this.s3ResourcesClient = s3ResourcesClient;
        this.s3BatchesClient = s3BatchesClient;
        this.s3OutputClient = s3OutputClient;
        this.queueClient = queueClient;
    }

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        logger.info("Received event: {}", sqsEvent);
        getRecords(sqsEvent).ifPresentOrElse(this::processEvents, this::createInitialEvent);
        return null;
    }

    protected void processInput(KeyBatchRequestEvent input) {
        var continuationToken = getContinuationToken(input);
        var location = getLocation(input);
        var batchResponse = fetchSingleBatch(continuationToken);

        if (batchResponse.isTruncated()) {
            sendNewKeyBatchEvent(batchResponse.getNextContinuationToken(), location);
        } else {
            logger.info("Last batch: {}", batchResponse.getKey());
        }

        batchResponse.getKey()
            .map(this::extractContent)
            .filter(keys -> !keys.isEmpty())
            .map(this::mapToIndexDocuments)
            .map(this::aggregateNquads)
            .map(nquads -> attempt(() -> compress(nquads)).orElseThrow())
            .map(this::persistNquads);

        logger.info(LAST_CONSUMED_BATCH, batchResponse.getKey());
    }

    private static Optional<List<KeyBatchRequestEvent>> getRecords(SQSEvent sqsEvent) {
        return isNull(sqsEvent.getRecords()) || sqsEvent.getRecords().isEmpty()
                   ? Optional.empty()
                   : Optional.of(sqsEvent.getRecords()
                                     .stream()
                                     .map(sqsMessage -> KeyBatchRequestEvent.fromJsonString(sqsMessage.getBody()))
                                     .collect(Collectors.toList()));
    }

    @JacocoGenerated
    private static AwsSqsClient defaultSqsClient(Environment environment) {
        var region = environment.readEnv(REGION);
        var queueUrl = environment.readEnv(QUEUE_URL);
        return new AwsSqsClient(Region.of(region), queueUrl);
    }

    private static String getContinuationToken(KeyBatchRequestEvent input) {
        return nonNull(input) && nonNull(input.getContinuationToken()) ? input.getContinuationToken() : null;
    }

    @JacocoGenerated
    private static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
    }

    private static URI extractGraphName(JsonNode content) {
        var id = content.at(ID_POINTER);
        if (id.isMissingNode()) {
            logger.error(MISSING_ID_NODE_IN_CONTENT_ERROR, content);
            throw new MissingIdException();
        }
        return URI.create(id.textValue() + NT_EXTENSION);
    }

    private void createInitialEvent() {
        processInput(new KeyBatchRequestEvent());
    }

    private void processEvents(List<KeyBatchRequestEvent> records) {
        records.forEach(this::processInput);
    }

    private void sendNewKeyBatchEvent(String continuationToken, String location) {
        logger.info("Sending event with continuation token: {}", continuationToken);
        queueClient.sendMessage(new KeyBatchRequestEvent(continuationToken, location).toJsonString());
    }

    private String aggregateNquads(Stream<JsonNode> element) {
        return element.map(this::mapToNquads)
                   .collect(Collectors.joining(System.lineSeparator()));
    }

    private boolean persistNquads(byte[] nquads) {
        var request = PutObjectRequest.builder()
                          .bucket(ENVIRONMENT.readEnv(LOADER_BUCKET))
                          .key(UUID.randomUUID() + NQUADS_GZIPPED)
                          .build();
        var response = s3OutputClient.putObject(request, RequestBody.fromBytes(nquads));
        return response.sdkHttpResponse().isSuccessful();
    }

    private String mapToNquads(JsonNode content) {
        return Nquads.transform(content.toString(), extractGraphName(content)).toString();
    }

    private String extractContent(String key) {
        var s3Driver = new S3Driver(s3BatchesClient, KEY_BATCHES_BUCKET);
        logger.info(PROCESSING_BATCH_MESSAGE, key);
        return attempt(() -> s3Driver.getFile(UnixPath.of(key))).orElseThrow();
    }

    private String getLocation(KeyBatchRequestEvent input) {
        return nonNull(input) && nonNull(input.getLocation()) ? input.getLocation() : null;
    }

    private ListingResponse fetchSingleBatch(String continuationToken) {
        var response = s3BatchesClient.listObjectsV2(
            ListObjectsV2Request.builder()
                .bucket(KEY_BATCHES_BUCKET)
                .continuationToken(continuationToken)
                .maxKeys(1)
                .build());
        return new ListingResponse(response);
    }

    private Stream<JsonNode> mapToIndexDocuments(String content) {
        return extractIdentifiers(content)
                   .filter(Objects::nonNull)
                   .map(this::fetchS3Content)
                   .map(this::unwrap);
    }

    private JsonNode unwrap(String json) {
        return attempt(() -> new DocumentUnwrapper(API_HOST).unwrap(json)).orElseThrow();
    }

    private Stream<String> extractIdentifiers(String value) {
        return nonNull(value) && !value.isBlank()
                   ? Arrays.stream(value.split(LINE_BREAK))
                   : Stream.empty();
    }

    private String fetchS3Content(String key) {
        logger.info("Fetching content for key: {}", key);
        var s3Driver = new S3Driver(s3ResourcesClient, ENVIRONMENT.readEnv(EXPANDED_RESOURCES_BUCKET));
        return attempt(() -> s3Driver.getFile(UnixPath.of(key))).orElseThrow();
    }

    private static class ListingResponse {

        private final boolean truncated;
        private final String key;
        private final String nextContinuationToken;

        public ListingResponse(ListObjectsV2Response response) {
            this.truncated = Boolean.TRUE.equals(response.isTruncated());
            this.key = extractKey(response);
            this.nextContinuationToken = response.nextContinuationToken();
        }

        public String getNextContinuationToken() {
            return nextContinuationToken;
        }

        public boolean isTruncated() {
            return truncated;
        }

        public Optional<String> getKey() {
            return Optional.ofNullable(key);
        }

        private static String extractKey(ListObjectsV2Response response) {
            var contents = response.contents();
            return contents.isEmpty() ? null : contents.getFirst().key();
        }
    }
}


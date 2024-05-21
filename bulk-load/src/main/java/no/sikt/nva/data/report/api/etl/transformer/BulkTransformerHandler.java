package no.sikt.nva.data.report.api.etl.transformer;

import static java.util.Objects.nonNull;
import static no.sikt.nva.data.report.api.etl.transformer.util.GzipUtil.compress;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import commons.db.utils.DocumentUnwrapper;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class BulkTransformerHandler extends EventHandler<KeyBatchRequestEvent, Void> {

    private static final Logger logger = LoggerFactory.getLogger(BulkTransformerHandler.class);
    private static final Environment ENVIRONMENT = new Environment();
    public static final String API_HOST = ENVIRONMENT.readEnv("API_HOST");
    private static final String MANDATORY_UNUSED_SUBTOPIC = "DETAIL.WITH.TOPIC";
    private static final String LOADER_BUCKET = "LOADER_BUCKET";
    private static final String EXPANDED_RESOURCES_BUCKET = "EXPANDED_RESOURCES_BUCKET";
    private static final String NQUADS_GZIPPED = ".nquads.gz";
    private static final String KEY_BATCHES_BUCKET
        = ENVIRONMENT.readEnv("KEY_BATCHES_BUCKET");
    private static final String EVENT_BUS = ENVIRONMENT.readEnv("EVENT_BUS");
    private static final String TOPIC = ENVIRONMENT.readEnv("TOPIC");
    private static final String PROCESSING_BATCH_MESSAGE = "Processing batch: {}";
    private static final String LAST_CONSUMED_BATCH = "Processed batch batch: {}";
    private static final String LINE_BREAK = "\n";
    private static final String ID_POINTER = "/id";
    private static final String NT_EXTENSION = ".nt";
    private static final String MISSING_ID_NODE_IN_CONTENT_ERROR = "Missing id-node in content: {}";
    private final S3Client s3ResourcesClient;
    private final S3Client s3BatchesClient;
    private final S3Client s3OutputClient;
    private final EventBridgeClient eventBridgeClient;

    @JacocoGenerated
    public BulkTransformerHandler() {
        this(defaultS3Client(), defaultS3Client(), defaultS3Client(), defaultEventBridgeClient());
    }

    public BulkTransformerHandler(S3Client s3ResourcesClient,
                                  S3Client s3BatchesClient,
                                  S3Client s3OutputClient,
                                  EventBridgeClient eventBridgeClient) {
        super(KeyBatchRequestEvent.class);
        this.s3ResourcesClient = s3ResourcesClient;
        this.s3BatchesClient = s3BatchesClient;
        this.s3OutputClient = s3OutputClient;
        this.eventBridgeClient = eventBridgeClient;
    }

    @Override
    protected Void processInput(KeyBatchRequestEvent input,
                                AwsEventBridgeEvent<KeyBatchRequestEvent> event,
                                Context context) {
        var continuationToken = getContinuationToken(input);
        var location = getLocation(input);
        var batchResponse = fetchSingleBatch(continuationToken);

        emitNextEvent(batchResponse, location, context);

        batchResponse.getKey()
            .map(this::extractContent)
            .filter(keys -> !keys.isEmpty())
            .map(this::mapToIndexDocuments)
            .map(this::aggregateNquads)
            .map(nquads -> attempt(() -> compress(nquads)).orElseThrow())
            .map(this::persistNquads);

        logger.info(LAST_CONSUMED_BATCH, batchResponse.getKey());
        return null;
    }

    private static PutEventsRequestEntry constructRequestEntry(ListingResponse batchResponse,
                                                               String location,
                                                               Context context) {
        return PutEventsRequestEntry.builder()
                   .eventBusName(EVENT_BUS)
                   .detail(new KeyBatchRequestEvent(batchResponse.getKey().orElse(null),
                                                    batchResponse.getContinuationToken(), TOPIC,
                                                    location).toJsonString())
                   .detailType(MANDATORY_UNUSED_SUBTOPIC)
                   .source(BulkTransformerHandler.class.getName())
                   .resources(context.getInvokedFunctionArn())
                   .time(Instant.now())
                   .build();
    }

    private static String getContinuationToken(KeyBatchRequestEvent input) {
        return nonNull(input) && nonNull(input.getContinuationToken()) ? input.getContinuationToken() : null;
    }

    @JacocoGenerated
    private static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
    }

    @JacocoGenerated
    private static EventBridgeClient defaultEventBridgeClient() {
        return EventBridgeClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
    }

    private static URI extractGraphName(JsonNode content) {
        var id = content.at(ID_POINTER);
        if (id.isMissingNode()) {
            logger.error(MISSING_ID_NODE_IN_CONTENT_ERROR, content);
            throw new MissingIdException();
        }
        return URI.create(id.textValue() + NT_EXTENSION);
    }

    private String aggregateNquads(Stream<JsonNode> element) {
        return element.map(this::mapToNquads)
                   .collect(Collectors.joining(System.lineSeparator()));
    }

    private void emitNextEvent(ListingResponse batchResponse,
                               String location,
                               Context context) {
        if (batchResponse.isTruncated()) {
            logger.info("Emitting event for next batch. Start marker: {}. Continuation token: {}",
                        batchResponse.getKey(), batchResponse.getContinuationToken());
            sendEvent(constructRequestEntry(batchResponse, location, context));
        }
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
        logger.info("Fetching batch with continuationToken: {}", continuationToken);
        var response = s3BatchesClient.listObjectsV2(
            ListObjectsV2Request.builder()
                .bucket(KEY_BATCHES_BUCKET)
                .continuationToken(continuationToken)
                .maxKeys(1)
                .build());
        if (!response.contents().isEmpty()) {
            logger.info("Response content key: {}", response.contents().getFirst().key());
            logger.info("Response isTruncated: {}, continuation token: {}", response.isTruncated(),
                        response.continuationToken());
        }
        return new ListingResponse(response);
    }

    private void sendEvent(PutEventsRequestEntry event) {
        eventBridgeClient.putEvents(PutEventsRequest.builder().entries(event).build());
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
        private final String continuationToken;

        public ListingResponse(ListObjectsV2Response response) {
            this.truncated = Boolean.TRUE.equals(response.isTruncated());
            this.key = extractKey(response);
            this.continuationToken = response.continuationToken();
        }

        public String getContinuationToken() {
            return continuationToken;
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


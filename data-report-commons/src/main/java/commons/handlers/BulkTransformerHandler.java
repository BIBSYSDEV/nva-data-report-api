package commons.handlers;

import static commons.utils.GzipUtil.compress;
import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import commons.db.utils.DocumentUnwrapper;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

public abstract class BulkTransformerHandler extends EventHandler<KeyBatchRequestEvent, Void> {

    private static final Logger logger = LoggerFactory.getLogger(BulkTransformerHandler.class);
    private static final Environment ENVIRONMENT = new Environment();
    private static final String API_HOST = ENVIRONMENT.readEnv("API_HOST");
    private static final String MANDATORY_UNUSED_SUBTOPIC = "DETAIL.WITH.TOPIC";
    private static final String EXPANDED_RESOURCES_BUCKET = "EXPANDED_RESOURCES_BUCKET";
    private static final String KEY_BATCHES_BUCKET
        = ENVIRONMENT.readEnv("KEY_BATCHES_BUCKET");
    private static final String EVENT_BUS = ENVIRONMENT.readEnv("EVENT_BUS");
    private static final String TOPIC = ENVIRONMENT.readEnv("TOPIC");
    private static final String PROCESSING_BATCH_MESSAGE = "Processing batch: {}";
    private static final String LAST_CONSUMED_BATCH = "Last consumed batch: {}";
    private static final String LINE_BREAK = "\n";
    private final S3Client s3ResourcesClient;
    private final S3Client s3BatchesClient;
    private final EventBridgeClient eventBridgeClient;

    @JacocoGenerated
    public BulkTransformerHandler() {
        this(defaultS3Client(), defaultS3Client(), defaultEventBridgeClient());
    }

    public BulkTransformerHandler(S3Client s3ResourcesClient,
                                  S3Client s3BatchesClient,
                                  EventBridgeClient eventBridgeClient) {
        super(KeyBatchRequestEvent.class);
        this.s3ResourcesClient = s3ResourcesClient;
        this.s3BatchesClient = s3BatchesClient;
        this.eventBridgeClient = eventBridgeClient;
    }

    @Override
    protected Void processInput(KeyBatchRequestEvent input,
                                AwsEventBridgeEvent<KeyBatchRequestEvent> event,
                                Context context) {
        var startMarker = getStartMarker(input);
        var location = getLocation(input);
        var batchResponse = fetchSingleBatch(startMarker);

        emitNextEvent(batchResponse, location, context);

        batchResponse.getKey()
            .map(this::extractContent)
            .filter(keys -> !keys.isEmpty())
            .map(this::mapToIndexDocuments)
            .map(this::processBatch)
            .map(transformedData -> attempt(() -> compress(transformedData)).orElseThrow())
            .map(this::persist);

        logger.info(LAST_CONSUMED_BATCH, batchResponse.getKey());
        return null;
    }

    public abstract String processBatch(Stream<JsonNode> jsonNodeStream);

    public abstract boolean persist(byte[] data);

    private static PutEventsRequestEntry constructRequestEntry(String lastEvaluatedKey,
                                                               String location,
                                                               Context context) {
        return PutEventsRequestEntry.builder()
                   .eventBusName(EVENT_BUS)
                   .detail(new KeyBatchRequestEvent(lastEvaluatedKey, TOPIC, location).toJsonString())
                   .detailType(MANDATORY_UNUSED_SUBTOPIC)
                   .source(BulkTransformerHandler.class.getName())
                   .resources(context.getInvokedFunctionArn())
                   .time(Instant.now())
                   .build();
    }

    private static String getStartMarker(KeyBatchRequestEvent input) {
        return nonNull(input) && nonNull(input.getStartMarker()) ? input.getStartMarker() : null;
    }

    @JacocoGenerated
    private static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
    }

    @JacocoGenerated
    private static EventBridgeClient defaultEventBridgeClient() {
        return EventBridgeClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
    }

    private void emitNextEvent(ListingResponse batchResponse,
                               String location,
                               Context context) {
        if (batchResponse.isTruncated()) {
            sendEvent(constructRequestEntry(batchResponse.getKey().orElse(null),
                                            location,
                                            context));
        }
    }

    private String extractContent(String key) {
        var s3Driver = new S3Driver(s3BatchesClient, KEY_BATCHES_BUCKET);
        logger.info(PROCESSING_BATCH_MESSAGE, key);
        return attempt(() -> s3Driver.getFile(UnixPath.of(key))).orElseThrow();
    }

    private String getLocation(KeyBatchRequestEvent input) {
        return nonNull(input) && nonNull(input.getLocation()) ? input.getLocation() : null;
    }

    private ListingResponse fetchSingleBatch(String startMarker) {
        var response = s3BatchesClient.listObjectsV2(
            ListObjectsV2Request.builder()
                .bucket(KEY_BATCHES_BUCKET)
                .startAfter(startMarker)
                .maxKeys(1)
                .build());
        return new ListingResponse(response);
    }

    private void sendEvent(PutEventsRequestEntry event) {
        eventBridgeClient.putEvents(PutEventsRequest.builder().entries(event).build());
    }

    private Stream<JsonNode> mapToIndexDocuments(String content) {
        return extractIdentifiers(content)
                   .filter(Objects::nonNull)
                   .map(this::fetchS3Content)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
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

    private Optional<String> fetchS3Content(String key) {
        logger.info("Fetching content for key: {}", key);
        var s3Driver = new S3Driver(s3ResourcesClient, ENVIRONMENT.readEnv(EXPANDED_RESOURCES_BUCKET));
        try {
            return Optional.of(s3Driver.getFile(UnixPath.of(key)));
        } catch (NoSuchKeyException noSuchKeyException) {
            logger.info("Key not found: {}", key);
            return Optional.empty();
        }
    }

    private static class ListingResponse {

        private final boolean truncated;
        private final String key;

        public ListingResponse(ListObjectsV2Response response) {
            this.truncated = Boolean.TRUE.equals(response.isTruncated());
            this.key = extractKey(response);
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


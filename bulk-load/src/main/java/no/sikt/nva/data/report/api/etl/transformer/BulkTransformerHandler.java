package no.sikt.nva.data.report.api.etl.transformer;

import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import commons.db.utils.DocumentUnwrapper;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
    private static final String MANDATORY_UNUSED_SUBTOPIC = "DETAIL.WITH.TOPIC";
    private static final String LOADER_BUCKET = "LOADER_BUCKET";
    private static final String EXPANDED_RESOURCES_BUCKET = "EXPANDED_RESOURCES_BUCKET";
    private static final String NQUADS = ".nquads";
    private static final String KEY_BATCHES_BUCKET
        = ENVIRONMENT.readEnv("KEY_BATCHES_BUCKET");
    private static final String EVENT_BUS = ENVIRONMENT.readEnv("EVENT_BUS");
    private static final String TOPIC = ENVIRONMENT.readEnv("TOPIC");
    private static final String PROCESSING_BATCH_MESSAGE = "Processing batch: {}";
    private static final String LAST_CONSUMED_BATCH = "Last consumed batch: {}";
    private static final String LINE_BREAK = "\n";
    private static final String ID_POINTER = "/id";
    private static final String NT_EXTENSION = ".nt";

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
        var startMarker = getStartMarker(input);
        var location = getLocation(input);
        var batchResponse = fetchSingleBatch(startMarker);

        var batchKey = batchResponse.contents().getFirst().key();
        if (Boolean.TRUE.equals(batchResponse.isTruncated())) {
            sendEvent(constructRequestEntry(batchKey, context, location));
        }

        var keys = extractContent(batchKey);
        var nquads = mapToIndexDocuments(keys).stream()
                         .map(this::mapToNquads)
                         .collect(Collectors.joining(System.lineSeparator()));
        if (!nquads.isEmpty()) {
            persistNquads(nquads);
        }
        logger.info(LAST_CONSUMED_BATCH, batchResponse.contents().getFirst());
        return null;
    }

    private static PutEventsRequestEntry constructRequestEntry(String lastEvaluatedKey,
                                                               Context context,
                                                               String location) {
        return PutEventsRequestEntry.builder()
                   .eventBusName(EVENT_BUS)
                   .detail(new KeyBatchRequestEvent(lastEvaluatedKey, TOPIC, location)
                               .toJsonString())
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

    private void persistNquads(String nquads) {
        var request = PutObjectRequest.builder()
                          .bucket(ENVIRONMENT.readEnv(LOADER_BUCKET))
                          .key(UUID.randomUUID() + NQUADS)
                          .build();
        s3OutputClient.putObject(request, RequestBody.fromString(nquads));
    }

    private String mapToNquads(JsonNode content) {
        var graphName = URI.create(content.at(ID_POINTER).textValue() + NT_EXTENSION);
        return Nquads.transform(content.toString(), graphName).toString();
    }

    private String extractContent(String key) {
        var s3Driver = new S3Driver(s3BatchesClient, KEY_BATCHES_BUCKET);
        logger.info(PROCESSING_BATCH_MESSAGE, key);
        return attempt(() -> s3Driver.getFile(UnixPath.of(key))).orElseThrow();
    }

    private String getLocation(KeyBatchRequestEvent input) {
        return nonNull(input) && nonNull(input.getLocation()) ? input.getLocation() : null;
    }

    private ListObjectsV2Response fetchSingleBatch(String startMarker) {
        return s3BatchesClient.listObjectsV2(
            ListObjectsV2Request.builder()
                .bucket(KEY_BATCHES_BUCKET)
                .startAfter(startMarker)
                .maxKeys(1)
                .build());
    }

    private void sendEvent(PutEventsRequestEntry event) {
        eventBridgeClient.putEvents(PutEventsRequest.builder().entries(event).build());
    }

    private List<JsonNode> mapToIndexDocuments(String content) {
        return extractIdentifiers(content)
                   .filter(Objects::nonNull)
                   .map(this::fetchS3Content)
                   .map(this::unwrap)
                   .toList();
    }

    private JsonNode unwrap(String json) {
        return attempt(() -> DocumentUnwrapper.unwrap(json)).orElseThrow();
    }

    private Stream<String> extractIdentifiers(String value) {
        return nonNull(value) && !value.isBlank()
                   ? Arrays.stream(value.split(LINE_BREAK))
                   : Stream.empty();
    }

    private String fetchS3Content(String key) {
        var s3Driver = new S3Driver(s3ResourcesClient, ENVIRONMENT.readEnv(EXPANDED_RESOURCES_BUCKET));
        return attempt(() -> s3Driver.getFile(UnixPath.of(key))).orElseThrow();
    }
}

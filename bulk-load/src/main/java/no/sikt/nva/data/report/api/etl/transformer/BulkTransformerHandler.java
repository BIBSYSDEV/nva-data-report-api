package no.sikt.nva.data.report.api.etl.transformer;

import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import commons.db.utils.GraphName;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.sikt.nva.data.report.api.etl.transformer.model.IndexDocument;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.paths.UnixPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
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
    private static final String NQUADS = ".nquads";
    private static final String KEY_BATCHES_BUCKET
        = ENVIRONMENT.readEnv("KEY_BATCHES_BUCKET");
    private static final String EVENT_BUS = ENVIRONMENT.readEnv("EVENT_BUS");
    private static final String TOPIC = ENVIRONMENT.readEnv("TOPIC");
    private static final String PROCESSING_BATCH_MESSAGE = "Processing batch: {}";
    private static final String LAST_CONSUMED_BATCH = "Last consumed batch: {}";
    private static final String LINE_BREAK = "\n";

    private final S3Client s3ResourcesClient;
    private final S3Client s3BatchesClient;
    private final S3Client s3OutputClient;
    private final EventBridgeClient eventBridgeClient;

    // TODO: Default constructor
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
        var nquads = mapToIndexDocuments(keys, location).stream()
                         .map(this::mapToNquads)
                         .collect(Collectors.joining(System.lineSeparator()));
        if (!nquads.isEmpty()) {
            persistNquads(nquads);
        }
        logger.info(LAST_CONSUMED_BATCH, batchResponse.contents().getFirst());
        return null;
    }

    private void persistNquads(String nquads) {
        var request = PutObjectRequest.builder()
                          .bucket(ENVIRONMENT.readEnv(LOADER_BUCKET))
                          .key(UUID.randomUUID() + NQUADS)
                          .build();
        s3OutputClient.putObject(request, RequestBody.fromString(nquads));
    }

    private String mapToNquads(IndexDocument content) {
        // TODO: Refactor GraphName to allow construction of path of GraphName.
        // This is a silly workaround to make GraphName work.
        var documentIdentifier = content.getResource().at("/id").toString() + ".nt";
        var graphName = GraphName.newBuilder()
                            .withBase(ENVIRONMENT.readEnv("API_HOST"))
                            .fromUnixPath(UnixPath.of(documentIdentifier))
                            .build().toUri();
        var body = content.getResource().toString();
        return Nquads.transform(body, graphName).toString();
    }

    private static PutEventsRequestEntry constructRequestEntry(String lastEvaluatedKey, Context context,
                                                               String location) {
        return PutEventsRequestEntry.builder()
                   .eventBusName(EVENT_BUS)
                   .detail(new KeyBatchRequestEvent(lastEvaluatedKey, TOPIC, location).toJsonString())
                   .detailType(MANDATORY_UNUSED_SUBTOPIC)
                   .source(BulkTransformerHandler.class.getName())
                   .resources(context.getInvokedFunctionArn())
                   .time(Instant.now())
                   .build();
    }

    private String extractContent(String key) {
        var s3Driver = new S3Driver(s3BatchesClient, KEY_BATCHES_BUCKET);
        logger.info(PROCESSING_BATCH_MESSAGE, key);
        return attempt(() -> s3Driver.getFile(UnixPath.of(key))).orElseThrow();
    }

    private String getLocation(KeyBatchRequestEvent input) {
        return nonNull(input) && nonNull(input.getLocation()) ? input.getLocation() : null;
    }

    private static String getStartMarker(KeyBatchRequestEvent input) {
        return nonNull(input) && nonNull(input.getStartMarker()) ? input.getStartMarker() : null;
    }

    private ListObjectsV2Response fetchSingleBatch(String startMarker) {
        return s3BatchesClient.listObjectsV2(
            ListObjectsV2Request.builder().bucket(KEY_BATCHES_BUCKET).startAfter(startMarker).maxKeys(1).build());
    }

    private void sendEvent(PutEventsRequestEntry event) {
        eventBridgeClient.putEvents(PutEventsRequest.builder().entries(event).build());
    }

    private List<IndexDocument> mapToIndexDocuments(String content, String location) {
        return extractIdentifiers(content)
                   .filter(Objects::nonNull)
                   .map(key -> fetchS3Content(key, location))
                   .map(IndexDocument::fromJsonString)
                   .toList();
    }

    private Stream<String> extractIdentifiers(String value) {
        return nonNull(value) && !value.isBlank()
                   ? Arrays.stream(value.split(LINE_BREAK))
                   : Stream.empty();
    }

    private String fetchS3Content(String key, String location) {
        var s3Driver = new S3Driver(s3ResourcesClient, location);
        return attempt(() -> s3Driver.getFile(UnixPath.of(key))).orElseThrow();
    }
}

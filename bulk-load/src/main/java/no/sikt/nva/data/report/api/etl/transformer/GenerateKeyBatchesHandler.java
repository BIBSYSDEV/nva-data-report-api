package no.sikt.nva.data.report.api.etl.transformer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import com.amazonaws.services.lambda.runtime.Context;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

public class GenerateKeyBatchesHandler extends EventHandler<KeyBatchRequestEvent, Void> {

    private static final String DEFAULT_BATCH_SIZE = "1000";
    private static final String DELIMITER = "/";
    private static final String DEFAULT_START_MARKER = null;
    private static final String INFO_MESSAGE = "Start marker: {}. Location: {}";
    private static final String MANDATORY_UNUSED_SUBTOPIC = "DETAIL.WITH.TOPIC";
    private static final Logger logger = LoggerFactory.getLogger(GenerateKeyBatchesHandler.class);
    private static final Environment ENVIRONMENT = new Environment();
    private static final String INPUT_BUCKET = ENVIRONMENT.readEnv("EXPANDED_RESOURCES_BUCKET");
    private static final String OUTPUT_BUCKET = ENVIRONMENT.readEnv("KEY_BATCHES_BUCKET");
    private static final String EVENT_BUS = ENVIRONMENT.readEnv("EVENT_BUS");
    private static final String TOPIC = ENVIRONMENT.readEnv("TOPIC");
    private static final int MAX_KEYS = Integer.parseInt(
        ENVIRONMENT.readEnvOpt("BATCH_SIZE").orElse(DEFAULT_BATCH_SIZE));
    private static final String DEFAULT_LOCATION = "resources";
    private static final String LAST_KEY_IN_BATCH_MESSAGE = "Last key in batch: {}";
    private static final String WROTE_ITEMS_MESSAGE = "Wrote {} items to {}";
    private final S3Client inputClient;
    private final S3Client outputClient;
    private final EventBridgeClient eventBridgeClient;

    @JacocoGenerated
    public GenerateKeyBatchesHandler() {
        this(defaultS3Client(), defaultS3Client(), defaultEventBridgeClient());
    }

    public GenerateKeyBatchesHandler(S3Client inputClient,
                                     S3Client outputClient,
                                     EventBridgeClient eventBridgeClient) {
        super(KeyBatchRequestEvent.class);
        this.inputClient = inputClient;
        this.outputClient = outputClient;
        this.eventBridgeClient = eventBridgeClient;
    }

    @JacocoGenerated
    public static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
    }

    @Override
    protected Void processInput(KeyBatchRequestEvent input,
                                AwsEventBridgeEvent<KeyBatchRequestEvent> event,
                                Context context) {
        var startMarker = getStartMarker(input);
        var location = getLocation(input);
        logger.info(INFO_MESSAGE, startMarker, location);
        var request = createRequest(startMarker, location);
        logger.info("Requesting data from {}", request.bucket());
        var response = inputClient.listObjectsV2(request);
        logger.info(LAST_KEY_IN_BATCH_MESSAGE, response.contents().getLast());
        var keys = getKeys(response);
        writeObject(location, toKeyString(keys));
        logger.info(WROTE_ITEMS_MESSAGE, keys.size(), OUTPUT_BUCKET);
        var lastEvaluatedKey = getLastEvaluatedKey(keys);
        var eventsResponse = sendEvent(constructRequestEntry(lastEvaluatedKey, context, location));
        logger.info(eventsResponse.toString());
        return null;
    }

    private static boolean isNotEmptyEvent(KeyBatchRequestEvent event) {
        return nonNull(event) && nonNull(event.getLocation());
    }

    private static PutEventsRequestEntry constructRequestEntry(String lastEvaluatedKey,
                                                               Context context,
                                                               String location) {
        return PutEventsRequestEntry.builder()
                   .eventBusName(EVENT_BUS)
                   .detail(new KeyBatchRequestEvent(lastEvaluatedKey, TOPIC, location)
                               .toJsonString())
                   .detailType(MANDATORY_UNUSED_SUBTOPIC)
                   // TODO: replace Object.class with actual class name
                   .source(Object.class.getName())
                   .resources(context.getInvokedFunctionArn())
                   .time(Instant.now())
                   .build();
    }

    private static String getStartMarker(KeyBatchRequestEvent input) {
        return notEmptyEvent(input) ? input.getStartMarker() : DEFAULT_START_MARKER;
    }

    private static boolean notEmptyEvent(KeyBatchRequestEvent event) {
        return nonNull(event) && nonNull(event.getStartMarker());
    }

    private static ListObjectsV2Request createRequest(String startMarker, String location) {
        return ListObjectsV2Request.builder()
                   .bucket(INPUT_BUCKET)
                   .prefix(location + DELIMITER)
                   .delimiter(DELIMITER)
                   .startAfter(startMarker)
                   .maxKeys(MAX_KEYS)
                   .build();
    }

    private static String toKeyString(List<String> values) {
        return values.stream().collect(Collectors.joining(System.lineSeparator()));
    }

    private static List<String> getKeys(ListObjectsV2Response response) {
        var commonPrefixes = response.commonPrefixes().stream()
                                                .map(CommonPrefix::prefix)
                                                .toList();
        return response.contents().stream()
                   .map(S3Object::key)
                   .filter(key -> excludeBucketFolder(key, commonPrefixes))
                   .toList();
    }

    private static boolean excludeBucketFolder(String key, List<String> commonPrefixes) {
        return !commonPrefixes.contains(key);
    }

    private static String getLastEvaluatedKey(List<String> keys) {
        return keys.getLast();
    }

    @JacocoGenerated
    private static EventBridgeClient defaultEventBridgeClient() {
        return EventBridgeClient.builder()
                   .httpClientBuilder(UrlConnectionHttpClient.builder())
                   .build();
    }

    private String getLocation(KeyBatchRequestEvent event) {
        return isNotEmptyEvent(event) ? event.getLocation() : DEFAULT_LOCATION;
    }

    private PutEventsResponse sendEvent(PutEventsRequestEntry event) {
        return eventBridgeClient.putEvents(PutEventsRequest.builder().entries(event).build());
    }

    private void writeObject(String location, String object) {
        var key = location + DELIMITER + randomUUID();
        var request = PutObjectRequest.builder()
                          .bucket(OUTPUT_BUCKET)
                          .key(key)
                          .build();
        outputClient.putObject(request, RequestBody.fromBytes(object.getBytes(UTF_8)));
    }
}

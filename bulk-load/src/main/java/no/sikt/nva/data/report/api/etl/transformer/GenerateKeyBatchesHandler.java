package no.sikt.nva.data.report.api.etl.transformer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.sikt.nva.data.report.api.etl.aws.AwsSqsClient;
import no.sikt.nva.data.report.api.etl.queue.MessageResponse;
import no.sikt.nva.data.report.api.etl.queue.QueueClient;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

public class GenerateKeyBatchesHandler implements RequestHandler<SQSEvent, Void> {

    private static final Logger logger = LoggerFactory.getLogger(GenerateKeyBatchesHandler.class);
    private static final String DEFAULT_BATCH_SIZE = "1000";
    private static final String DELIMITER = "/";
    private static final String INFO_MESSAGE = "Continuation token: {}. Location: {}";
    private static final Environment ENVIRONMENT = new Environment();
    private static final String INPUT_BUCKET = ENVIRONMENT.readEnv("EXPANDED_RESOURCES_BUCKET");
    private static final String OUTPUT_BUCKET = ENVIRONMENT.readEnv("KEY_BATCHES_BUCKET");
    private static final int MAX_KEYS = Integer.parseInt(
        ENVIRONMENT.readEnvOpt("BATCH_SIZE").orElse(DEFAULT_BATCH_SIZE));
    private static final String NEXT_CONTINUATION_TOKEN = "Emitting new event. Next continuation token: {}";
    private static final String WROTE_ITEMS_MESSAGE = "Wrote {} items to {}";
    private static final String REGION = "AWS_REGION_NAME";
    private static final String QUEUE_URL = "KEY_BATCHES_QUEUE_URL";
    private final S3Client inputClient;
    private final S3Client outputClient;
    private final QueueClient queueClient;

    @JacocoGenerated
    public GenerateKeyBatchesHandler() {
        this(defaultS3Client(), defaultS3Client(), defaultSqsClient(new Environment()));
    }

    public GenerateKeyBatchesHandler(S3Client inputClient,
                                     S3Client outputClient,
                                     QueueClient queueClient) {
        this.inputClient = inputClient;
        this.outputClient = outputClient;
        this.queueClient = queueClient;
    }

    @JacocoGenerated
    public static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
    }

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        logger.info("Received event: {}", sqsEvent);
        getEvents(sqsEvent).ifPresentOrElse(this::processEvents, this::createInitialEvent);
        return null;
    }

    protected void processInput(KeyBatchRequestEvent input) {
        var location = input.getLocation();
        var request = createListObjectsRequest(input.getContinuationToken(), location);
        var response = listObjects(request);
        if (response.isTruncated()) {
            emitNextRequest(location, response.nextContinuationToken());
        } else {
            logger.info("Last batch: {}", extractKey(response));
        }
        getKeys(response).ifPresent(keys -> writeObject(location, keys));
    }

    private static String extractKey(ListObjectsV2Response response) {
        var contents = response.contents();
        return contents.isEmpty() ? null : contents.getFirst().key();
    }

    private static Optional<List<KeyBatchRequestEvent>> getEvents(SQSEvent sqsEvent) {
        return isNull(sqsEvent.getRecords()) || sqsEvent.getRecords().isEmpty()
                   ? Optional.empty()
                   : Optional.of(sqsEvent.getRecords()
                                     .stream()
                                     .map(sqsMessage -> KeyBatchRequestEvent.fromJsonString(sqsMessage.getBody()))
                                     .collect(Collectors.toList()));
    }

    private static ListObjectsV2Request createListObjectsRequest(String continuationToken, String location) {
        logger.info(INFO_MESSAGE, continuationToken, location);
        return createRequest(continuationToken, location);
    }

    private static KeyBatchRequestEvent constructRequestEntry(String continuationToken, String location) {
        return new KeyBatchRequestEvent(continuationToken, location);
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

    private static Optional<List<String>> getKeys(ListObjectsV2Response response) {
        var commonPrefixes = response.commonPrefixes().stream()
                                 .map(CommonPrefix::prefix)
                                 .toList();
        var keys = response.contents().stream()
                       .map(S3Object::key)
                       .filter(key -> excludeBucketFolder(key, commonPrefixes))
                       .toList();
        return keys.isEmpty() ? Optional.empty() : Optional.of(keys);
    }

    private static boolean excludeBucketFolder(String key, List<String> commonPrefixes) {
        return !commonPrefixes.contains(key);
    }

    @JacocoGenerated
    private static AwsSqsClient defaultSqsClient(Environment environment) {
        var region = environment.readEnv(REGION);
        var queueUrl = environment.readEnv(QUEUE_URL);
        return new AwsSqsClient(Region.of(region), queueUrl);
    }

    private void createInitialEvent() {
        processInput(new KeyBatchRequestEvent());
    }

    private void processEvents(List<KeyBatchRequestEvent> keyBatchRequestEvents) {
        keyBatchRequestEvents.forEach(this::processInput);
    }

    private ListObjectsV2Response listObjects(ListObjectsV2Request request) {
        logger.info("Requesting data from {}", request.bucket());
        return inputClient.listObjectsV2(request);
    }

    private void emitNextRequest(String location, String continuationToken) {
        logger.info(NEXT_CONTINUATION_TOKEN, continuationToken);
        var eventsResponse = sendEvent(constructRequestEntry(continuationToken, location));
        logger.info(eventsResponse.toString());
    }

    private MessageResponse sendEvent(KeyBatchRequestEvent event) {
        return queueClient.sendMessage(event.toJsonString());
    }

    private void writeObject(String location, List<String> keys) {
        var key = location + DELIMITER + randomUUID();
        var object = toKeyString(keys);
        var request = PutObjectRequest.builder()
                          .bucket(OUTPUT_BUCKET)
                          .key(key)
                          .build();
        outputClient.putObject(request, RequestBody.fromBytes(object.getBytes(UTF_8)));
        logger.info(WROTE_ITEMS_MESSAGE, keys.size(), OUTPUT_BUCKET);
    }
}

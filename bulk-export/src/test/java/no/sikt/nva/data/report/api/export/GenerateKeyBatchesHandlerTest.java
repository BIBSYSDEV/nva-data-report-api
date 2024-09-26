package no.sikt.nva.data.report.api.export;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.SingletonCollector;
import nva.commons.core.attempt.Try;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

class GenerateKeyBatchesHandlerTest {

    public static final String OUTPUT_BUCKET = "outputBucket";
    public static final int SINGLE_BATCH_FILE_SIZE = 10;
    private static final int MULTIPLE_BATCH_FILE_SIZE = 1001;
    public static final String DEFAULT_LOCATION = "resources";
    public static final ObjectMapper objectMapperWithEmpty = JsonUtils.dtoObjectMapper;
    private ByteArrayOutputStream outputStream;
    private FakeS3Client outputClient;
    private FakeS3Client inputClient;
    private StubEventBridgeClient eventBridgeClient;
    private GenerateKeyBatchesHandler handler;
    private S3Driver s3DriverInputBucket;
    private S3Driver s3DriverOutputBucket;

    @BeforeEach
    void setUp() {
        this.outputStream = new ByteArrayOutputStream();
        inputClient = new FakeS3Client();
        s3DriverInputBucket = new S3Driver(inputClient, "inputBucket");
        outputClient = new FakeS3Client();
        s3DriverOutputBucket = new S3Driver(outputClient, "outputBucket");
        eventBridgeClient = new StubEventBridgeClient();
        handler = new GenerateKeyBatchesHandler(inputClient, outputClient, eventBridgeClient);
    }

    @Test
    void shouldPersistS3KeysToBatchBucket() throws JsonProcessingException {
        final var allFiles = putObjectsInInputBucket(SINGLE_BATCH_FILE_SIZE, DEFAULT_LOCATION);

        handler.handleRequest(eventStream(DEFAULT_LOCATION), outputStream, mock(Context.class));

        var keys = getPersistedFileFromOutputBucket();
        var expected = allFiles.stream().collect(Collectors.joining(System.lineSeparator()));

        var actual = keys.stream().collect(Collectors.joining(System.lineSeparator()));
        assertEquals(expected, actual);
    }

    @Test
    void shouldReadGenerateBatchesFromS3LocationProvidedInEventBody() throws JsonProcessingException {
        var location = "requestedLocation";
        final var allFiles = putObjectsInInputBucket(SINGLE_BATCH_FILE_SIZE, location);
        putObjectsInInputBucket(SINGLE_BATCH_FILE_SIZE, "resources");

        handler.handleRequest(eventStream(location), outputStream, mock(Context.class));

        var keys = getPersistedFileFromOutputBucket();
        var expected = allFiles.stream().collect(Collectors.joining(System.lineSeparator()));

        var actual = keys.stream().collect(Collectors.joining(System.lineSeparator()));
        assertEquals(expected, actual);
    }

    @Test
    void shouldEmitNewEventWhenS3BucketHasNotBeenTruncated() throws JsonProcessingException {
        var s3Objects = putObjectsInInputBucket(MULTIPLE_BATCH_FILE_SIZE, DEFAULT_LOCATION);

        handler.handleRequest(eventStream(DEFAULT_LOCATION), outputStream, mock(Context.class));

        var emittedEvent = eventBridgeClient.getLatestEvent();

        var startMarkerForNextIteration = s3Objects.get(s3Objects.size() - 2);

        var actual = emittedEvent.getStartMarker();
        assertEquals(startMarkerForNextIteration, actual);
    }

    @Test
    void shouldProcessWithDefaultsWhenInputEventIsNull() {
        putObjectsInInputBucket(MULTIPLE_BATCH_FILE_SIZE, DEFAULT_LOCATION);
        assertDoesNotThrow(() -> handler.handleRequest(nullEventStream(),
                                                       outputStream,
                                                       mock(Context.class)));
    }

    private static String getBucketPath(UriWrapper uri) {
        return Path.of(UnixPath.fromString(uri.toString()).getPathElementByIndexFromEnd(1), uri.getLastPathElement())
                   .toString();
    }

    private InputStream eventStream(String location) throws JsonProcessingException {
        var event = new AwsEventBridgeEvent<KeyBatchRequestEvent>();
        event.setDetail(new KeyBatchRequestEvent(null, "topic", location));
        var jsonString = objectMapperWithEmpty.writeValueAsString(event);
        return IoUtils.stringToStream(jsonString);
    }

    private InputStream nullEventStream() throws JsonProcessingException {
        var event = new AwsEventBridgeEvent<KeyBatchRequestEvent>();
        event.setDetail(null);
        var jsonString = objectMapperWithEmpty.writeValueAsString(event);
        return IoUtils.stringToStream(jsonString);
    }

    private List<String> getPersistedFileFromOutputBucket() {
        var request = ListObjectsV2Request.builder().bucket(OUTPUT_BUCKET).maxKeys(10_000).build();
        var content = outputClient.listObjectsV2(request).contents();
        return content.stream().map(object -> s3DriverOutputBucket.getFile(UnixPath.of(object.key()))).toList();
    }

    private List<String> putObjectsInInputBucket(int numberOfItems, String location) {
        return IntStream.range(0, numberOfItems)
                   .mapToObj(item -> SortableIdentifier.next())
                   .map(SortableIdentifier::toString)
                   .map(key -> insertFileWithKey(key, location))
                   .map(UriWrapper::fromUri)
                   .map(GenerateKeyBatchesHandlerTest::getBucketPath)
                   .toList();
    }

    private URI insertFileWithKey(String key, String location) {
        return attempt(() -> s3DriverInputBucket.insertFile(UnixPath.of(location, key), randomString())).orElseThrow();
    }

    private static class StubEventBridgeClient implements EventBridgeClient {

        private KeyBatchRequestEvent latestEvent;

        public KeyBatchRequestEvent getLatestEvent() {
            return latestEvent;
        }

        public PutEventsResponse putEvents(PutEventsRequest putEventsRequest) {
            this.latestEvent = saveContainedEvent(putEventsRequest);
            return PutEventsResponse.builder().failedEntryCount(0).build();
        }

        @Override
        public String serviceName() {
            return null;
        }

        @Override
        public void close() {

        }

        private KeyBatchRequestEvent saveContainedEvent(PutEventsRequest putEventsRequest) {
            PutEventsRequestEntry eventEntry = putEventsRequest.entries()
                                                   .stream()
                                                   .collect(SingletonCollector.collect());
            return Try.attempt(eventEntry::detail).map(
                jsonString -> objectMapperWithEmpty.readValue(jsonString, KeyBatchRequestEvent.class)).orElseThrow();
        }
    }
}
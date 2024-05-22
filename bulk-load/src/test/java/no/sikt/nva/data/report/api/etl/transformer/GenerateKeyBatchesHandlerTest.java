package no.sikt.nva.data.report.api.etl.transformer;

import static no.sikt.nva.data.report.testing.utils.QueueServiceTestUtils.createEvent;
import static no.sikt.nva.data.report.testing.utils.QueueServiceTestUtils.emptyEvent;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.paths.UnixPath;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

class GenerateKeyBatchesHandlerTest {

    public static final String OUTPUT_BUCKET = "outputBucket";
    public static final int SINGLE_BATCH_FILE_SIZE = 10;
    public static final String DEFAULT_LOCATION = "resources";
    private static final int MULTIPLE_BATCH_FILE_SIZE = 1001;
    private FakeS3Client outputClient;
    private FakeSqsClient queueClient;
    private GenerateKeyBatchesHandler handler;
    private S3Driver s3DriverInputBucket;
    private S3Driver s3DriverOutputBucket;
    private Context context;

    @BeforeEach
    void setUp() {
        var inputClient = new FakeS3Client();
        s3DriverInputBucket = new S3Driver(inputClient, "inputBucket");
        outputClient = new FakeS3Client();
        s3DriverOutputBucket = new S3Driver(outputClient, "outputBucket");
        queueClient = new FakeSqsClient();
        handler = new GenerateKeyBatchesHandler(inputClient, outputClient, queueClient);
        context = mock(Context.class);
    }

    @AfterEach
    void tearDown() {
        queueClient.removeSentMessages();
    }

    @Test
    void shouldPersistS3KeysToBatchBucket() {
        final var allFiles = putObjectsInInputBucket(SINGLE_BATCH_FILE_SIZE, DEFAULT_LOCATION);

        handler.handleRequest(sqsEvent(DEFAULT_LOCATION), context);

        var keys = getPersistedFileFromOutputBucket();
        var expected = allFiles.stream().collect(Collectors.joining(System.lineSeparator()));

        var actual = keys.stream().collect(Collectors.joining(System.lineSeparator()));
        assertEquals(expected, actual);
    }

    @Test
    void shouldReadGenerateBatchesFromS3LocationProvidedInEventBody() {
        var location = "requestedLocation";
        final var allFiles = putObjectsInInputBucket(SINGLE_BATCH_FILE_SIZE, location);
        putObjectsInInputBucket(SINGLE_BATCH_FILE_SIZE, "resources");

        handler.handleRequest(sqsEvent(location), mock(Context.class));

        var keys = getPersistedFileFromOutputBucket();
        var expected = allFiles.stream().collect(Collectors.joining(System.lineSeparator()));

        var actual = keys.stream().collect(Collectors.joining(System.lineSeparator()));
        assertEquals(expected, actual);
    }

    @Test
    void shouldEmitNewEventWhenS3BucketHasNotBeenTruncated() throws JsonProcessingException {
        putObjectsInInputBucket(MULTIPLE_BATCH_FILE_SIZE, DEFAULT_LOCATION);

        handler.handleRequest(sqsEvent(DEFAULT_LOCATION), mock(Context.class));

        var emittedEvent = getEmittedEvent();

        assertNotNull(emittedEvent.getContinuationToken());
    }

    @Test
    void shouldNotEmitNewEventWhenS3BucketHasNotBeenTruncated() {
        putObjectsInInputBucket(SINGLE_BATCH_FILE_SIZE, DEFAULT_LOCATION);

        handler.handleRequest(sqsEvent(DEFAULT_LOCATION), mock(Context.class));

        assertEquals(0, queueClient.getSentMessages().size());
    }

    @Test
    void shouldProcessWithDefaultsWhenInputEventIsNull() {
        putObjectsInInputBucket(MULTIPLE_BATCH_FILE_SIZE, DEFAULT_LOCATION);
        assertDoesNotThrow(() -> handler.handleRequest(emptyEvent(), mock(Context.class)));
    }

    private static String getBucketPath(UriWrapper uri) {
        return Path.of(UnixPath.fromString(uri.toString()).getPathElementByIndexFromEnd(1), uri.getLastPathElement())
                   .toString();
    }

    private SQSEvent sqsEvent(String location) {
        var event = new KeyBatchRequestEvent(null, location);
        return createEvent(event.toJsonString());
    }

    private KeyBatchRequestEvent getEmittedEvent() throws JsonProcessingException {
        return dtoObjectMapper.readValue(queueClient.getSentMessages().getFirst().messageBody(),
                                         KeyBatchRequestEvent.class);
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
}
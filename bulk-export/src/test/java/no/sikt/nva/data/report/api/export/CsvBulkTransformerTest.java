package no.sikt.nva.data.report.api.export;

import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_ID;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import commons.handlers.KeyBatchRequestEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.sikt.nva.data.report.testing.utils.ResultSorter;
import no.sikt.nva.data.report.testing.utils.generator.TestData;
import no.sikt.nva.data.report.testing.utils.generator.TestData.DatePair;
import no.sikt.nva.data.report.testing.utils.generator.publication.PublicationDate;
import no.sikt.nva.data.report.testing.utils.model.EventConsumptionAttributes;
import no.sikt.nva.data.report.testing.utils.model.IndexDocument;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.Environment;
import nva.commons.core.SingletonCollector;
import nva.commons.core.StringUtils;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.s3.S3Client;

class CsvBulkTransformerTest {

    public static final String CSV = "CSV";
    private static final String DEFAULT_LOCATION = "resources";
    private static final Environment environment = new Environment();
    private S3Driver s3KeyBatches3Driver;
    private ByteArrayOutputStream outputStream;
    private S3Driver s3OutputDriver;
    private S3Driver s3ResourcesDriver;
    private EventBridgeClient eventBridgeClient;
    private CsvBulkTransformer handler;

    public static EventConsumptionAttributes randomConsumptionAttribute() {
        return new EventConsumptionAttributes(DEFAULT_LOCATION, SortableIdentifier.next());
    }

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        S3Client s3keyBatchClient = new FakeS3Client();
        s3KeyBatches3Driver = new S3Driver(s3keyBatchClient, environment.readEnv("KEY_BATCHES_BUCKET"));
        S3Client s3OutputClient = new FakeS3Client();
        s3OutputDriver = new S3Driver(s3OutputClient, environment.readEnv("EXPORT_BUCKET"));
        S3Client s3ResourcesClient = new FakeS3Client();
        s3ResourcesDriver = new S3Driver(s3ResourcesClient, environment.readEnv("EXPANDED_RESOURCES_BUCKET"));
        eventBridgeClient = new StubEventBridgeClient();
        handler = new CsvBulkTransformer(s3keyBatchClient, s3ResourcesClient, s3OutputClient, eventBridgeClient);
    }

    @Test
    void shouldWriteCsvFileToS3() throws IOException {
        var testData = new TestData(generateDatePairs(2));
        var indexDocuments = createAndPersistIndexDocuments(testData);
        var batch = indexDocuments.stream()
                        .map(IndexDocument::getDocumentIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var batchKey = randomString();
        s3KeyBatches3Driver.insertFile(UnixPath.of(batchKey), batch);
        handler.handleRequest(eventStream(null), outputStream, mock(Context.class));
        var actualContent = ResultSorter.sortResponse(CSV, getActualPersistedFile(), PUBLICATION_ID,
                                                      CONTRIBUTOR_IDENTIFIER);
        var expectedContent = testData.getPublicationResponseData();
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void shouldNotEmitNewEventWhenNoMoreBatchesToRetrieve() throws IOException {
        var testData = new TestData(generateDatePairs(2));
        var indexDocuments = createAndPersistIndexDocuments(testData);
        var batch = indexDocuments.stream()
                        .map(IndexDocument::getDocumentIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var batchKey = randomString();
        s3KeyBatches3Driver.insertFile(UnixPath.of(batchKey), batch);
        handler.handleRequest(eventStream(null), outputStream, Mockito.mock(Context.class));
        var emittedEvent = ((StubEventBridgeClient) eventBridgeClient).getLatestEvent();
        assertNull(emittedEvent);
    }

    @Test
    void shouldEmitNewEventWhenThereAreMoreBatchesToProcess() throws IOException {
        var testData = new TestData(generateDatePairs(2));
        var indexDocuments = createAndPersistIndexDocuments(testData);
        var batch = indexDocuments.stream()
                        .map(IndexDocument::getDocumentIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var batchKey = randomString();
        s3KeyBatches3Driver.insertFile(UnixPath.of(batchKey), batch);
        var expectedStarMarkerFromEmittedEvent = randomString();
        s3KeyBatches3Driver.insertFile(UnixPath.of(expectedStarMarkerFromEmittedEvent), batch);
        var list = new ArrayList<String>();
        list.add(null);
        list.add(batchKey);
        list.add(expectedStarMarkerFromEmittedEvent);
        for (var item : list) {
            handler.handleRequest(eventStream(item), outputStream, Mockito.mock(Context.class));

            var emittedEvent = ((StubEventBridgeClient) eventBridgeClient).getLatestEvent();

            assertEquals(batchKey, emittedEvent.getStartMarker());
        }
    }

    @Test
    void shouldNotFailWhenBlobNotFound() throws IOException {
        var testData = new TestData(generateDatePairs(2));
        var indexDocuments = createAndPersistIndexDocuments(testData);
        removeOneResourceFromPersistedResourcesBucket(indexDocuments);
        var batch = indexDocuments.stream()
                        .map(IndexDocument::getDocumentIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var batchKey = randomString();
        s3KeyBatches3Driver.insertFile(UnixPath.of(batchKey), batch);
        assertDoesNotThrow(() -> handler.handleRequest(eventStream(null), outputStream, Mockito.mock(Context.class)));
    }

    @Test
    void shouldSkipEmptyBatches() throws IOException {
        var batchKey = randomString();
        s3KeyBatches3Driver.insertFile(UnixPath.of(batchKey), StringUtils.EMPTY_STRING);
        handler.handleRequest(eventStream(null), outputStream, Mockito.mock(Context.class));

        var actual = s3OutputDriver.listAllFiles(UnixPath.of(""));
        assertEquals(0, actual.size());
    }

    List<DatePair> generateDatePairs(int numberOfDatePairs) {
        return IntStream.range(0, numberOfDatePairs)
                   .mapToObj(i -> new DatePair(new PublicationDate("2024", "02", "02"),
                                               Instant.now().minus(100, ChronoUnit.DAYS)))
                   .toList();
    }

    private void removeOneResourceFromPersistedResourcesBucket(List<IndexDocument> expectedDocuments) {
        var document = expectedDocuments.getFirst();
        s3ResourcesDriver.deleteFile(UnixPath.of(document.getDocumentIdentifier()));
    }

    private List<IndexDocument> createAndPersistIndexDocuments(TestData testData) {
        var indexDocuments = testData.getPublicationTestData().stream()
                                 .map(publication -> new IndexDocument(randomConsumptionAttribute(),
                                                                       PublicationIndexDocument.from(publication)
                                                                           .asJsonNode()))
                                 .toList();
        indexDocuments.forEach(document -> document.persistInS3(s3ResourcesDriver));
        return indexDocuments;
    }

    private String getActualPersistedFile() {
        var file = s3OutputDriver.listAllFiles(UnixPath.ROOT_PATH).getFirst();
        return s3OutputDriver.getFile(file);
    }

    private InputStream eventStream(String startMarker) throws JsonProcessingException {
        var event = new AwsEventBridgeEvent<KeyBatchRequestEvent>();
        event.setDetail(new KeyBatchRequestEvent(startMarker, randomString(), DEFAULT_LOCATION));
        event.setId(randomString());
        var jsonString = dtoObjectMapper.writeValueAsString(event);
        return IoUtils.stringToStream(jsonString);
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
            return attempt(eventEntry::detail).map(
                jsonString -> dtoObjectMapper.readValue(jsonString, KeyBatchRequestEvent.class)).orElseThrow();
        }
    }
}
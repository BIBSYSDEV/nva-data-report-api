package no.sikt.nva.data.report.api.export;

import static no.sikt.nva.data.report.testing.utils.ResultSorter.sortResponse;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_ID;
import static no.sikt.nva.data.report.testing.utils.model.ResultType.CSV;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import commons.handlers.KeyBatchRequestEvent;
import commons.model.ReportType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.sikt.nva.data.report.testing.utils.generator.TestData;
import no.sikt.nva.data.report.testing.utils.generator.TestData.DatePair;
import no.sikt.nva.data.report.testing.utils.generator.publication.PublicationDate;
import no.sikt.nva.data.report.testing.utils.model.EventConsumptionAttributes;
import no.sikt.nva.data.report.testing.utils.model.IndexDocument;
import no.sikt.nva.data.report.testing.utils.stubs.StubEventBridgeClient;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.Environment;
import nva.commons.core.StringUtils;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

class CsvTransformerTest {

    private static final String DEFAULT_LOCATION = "resources";
    private static final Environment environment = new Environment();
    private S3Driver s3KeyBatches3Driver;
    private ByteArrayOutputStream outputStream;
    private S3Driver s3OutputDriver;
    private S3Driver s3ResourcesDriver;
    private EventBridgeClient eventBridgeClient;
    private CsvTransformer handler;

    public static EventConsumptionAttributes randomConsumptionAttribute() {
        return new EventConsumptionAttributes(DEFAULT_LOCATION, SortableIdentifier.next());
    }

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        var s3keyBatchClient = new FakeS3Client();
        s3KeyBatches3Driver = new S3Driver(s3keyBatchClient, environment.readEnv("KEY_BATCHES_BUCKET"));
        var s3OutputClient = new FakeS3Client();
        s3OutputDriver = new S3Driver(s3OutputClient, environment.readEnv("EXPORT_BUCKET"));
        var s3ResourcesClient = new FakeS3Client();
        s3ResourcesDriver = new S3Driver(s3ResourcesClient, environment.readEnv("EXPANDED_RESOURCES_BUCKET"));
        eventBridgeClient = new StubEventBridgeClient();
        handler = new CsvTransformer(s3keyBatchClient, s3ResourcesClient, s3OutputClient, eventBridgeClient);
    }

    @ParameterizedTest
    @EnumSource(names = {"AFFILIATION", "CONTRIBUTOR", "FUNDING", "IDENTIFIER", "PUBLICATION"})
    void shouldWriteCsvFileToS3ForAllReportTypes(ReportType reportType) throws IOException {
        var testData = new TestData(generateDatePairs(2));
        var batch = setupExistingBatch(testData);
        var batchKey = randomString();
        s3KeyBatches3Driver.insertFile(UnixPath.of(batchKey), batch);
        handler.handleRequest(eventStream(null), outputStream, mock(Context.class));
        var actualContent = attempt(() -> sortResponse(CSV, getActualPersistedFile(reportType), PUBLICATION_ID,
                                                       CONTRIBUTOR_IDENTIFIER)).orElseThrow();
        var expectedContent = getExpectedResponseData(reportType, testData);
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void shouldWriteCsvFilesForAlleReportTypesToSpecificFolderInExportBucketWithCsvFileExtension() throws IOException {
        var testData = new TestData(generateDatePairs(1));
        var batch = setupExistingBatch(testData);
        var batchKey = randomString();
        s3KeyBatches3Driver.insertFile(UnixPath.of(batchKey), batch);
        handler.handleRequest(eventStream(null), outputStream, mock(Context.class));
        Arrays.stream(ReportType.values())
            .filter(reportType -> !reportType.equals(ReportType.NVI))//TODO: NVI is not supported yet
            .forEach(reportType -> {
                var expectedPath = UnixPath.of(reportType.getType());
                var file = s3OutputDriver.listAllFiles(expectedPath).getFirst();
                assertNotNull(file);
                assertTrue(file.getLastPathElement().contains(".csv"));
            });
    }

    @Test
    void shouldNotEmitNewEventWhenNoMoreBatchesToRetrieve() throws IOException {
        var testData = new TestData(generateDatePairs(2));
        var batch = setupExistingBatch(testData);
        var batchKey = randomString();
        s3KeyBatches3Driver.insertFile(UnixPath.of(batchKey), batch);
        handler.handleRequest(eventStream(null), outputStream, mock(Context.class));
        var emittedEvent = ((StubEventBridgeClient) eventBridgeClient).getLatestEvent();
        assertNull(emittedEvent);
    }

    @Test
    void shouldEmitNewEventWhenThereAreMoreBatchesToProcess() throws IOException {
        var testData = new TestData(generateDatePairs(2));
        var batch = setupExistingBatch(testData);
        var batchKey = randomString();
        s3KeyBatches3Driver.insertFile(UnixPath.of(batchKey), batch);
        var expectedStarMarkerFromEmittedEvent = randomString();
        s3KeyBatches3Driver.insertFile(UnixPath.of(expectedStarMarkerFromEmittedEvent), batch);
        var list = new ArrayList<String>();
        list.add(null);
        list.add(batchKey);
        list.add(expectedStarMarkerFromEmittedEvent);
        for (var item : list) {
            handler.handleRequest(eventStream(item), outputStream, mock(Context.class));

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
        assertDoesNotThrow(() -> handler.handleRequest(eventStream(null), outputStream, mock(Context.class)));
    }

    @Test
    void shouldSkipEmptyBatches() throws IOException {
        var batchKey = randomString();
        s3KeyBatches3Driver.insertFile(UnixPath.of(batchKey), StringUtils.EMPTY_STRING);
        handler.handleRequest(eventStream(null), outputStream, mock(Context.class));

        var actual = s3OutputDriver.listAllFiles(UnixPath.of(""));
        assertEquals(0, actual.size());
    }

    List<DatePair> generateDatePairs(int numberOfDatePairs) {
        return IntStream.range(0, numberOfDatePairs)
                   .mapToObj(i -> new DatePair(new PublicationDate("2024", "02", "02"),
                                               Instant.now().minus(100, ChronoUnit.DAYS)))
                   .toList();
    }

    private static String getExpectedResponseData(ReportType reportType, TestData test) {
        return switch (reportType) {
            case AFFILIATION -> test.getAffiliationResponseData();
            case CONTRIBUTOR -> test.getContributorResponseData();
            case FUNDING -> test.getFundingResponseData();
            case IDENTIFIER -> test.getIdentifierResponseData();
            case PUBLICATION -> test.getPublicationResponseData();
            case NVI -> null;
        };
    }

    private String setupExistingBatch(TestData testData) {
        var indexDocuments = createAndPersistIndexDocuments(testData);
        return indexDocuments.stream()
                   .map(IndexDocument::getDocumentIdentifier)
                   .collect(Collectors.joining(System.lineSeparator()));
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

    private String getActualPersistedFile(ReportType reportType) {
        var file = s3OutputDriver.listAllFiles(UnixPath.of(reportType.getType())).getFirst();
        return s3OutputDriver.getFile(file);
    }

    private InputStream eventStream(String startMarker) throws JsonProcessingException {
        var event = new AwsEventBridgeEvent<KeyBatchRequestEvent>();
        event.setDetail(new KeyBatchRequestEvent(startMarker, randomString(), DEFAULT_LOCATION));
        event.setId(randomString());
        var jsonString = dtoObjectMapper.writeValueAsString(event);
        return IoUtils.stringToStream(jsonString);
    }
}
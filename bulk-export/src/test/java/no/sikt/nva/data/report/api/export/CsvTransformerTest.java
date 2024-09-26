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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import commons.handlers.KeyBatchRequestEvent;
import commons.model.ReportType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.sikt.nva.data.report.testing.utils.generator.SampleData;
import no.sikt.nva.data.report.testing.utils.generator.SampleData.DatePair;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleNviCandidate;
import no.sikt.nva.data.report.testing.utils.generator.publication.PublicationDate;
import no.sikt.nva.data.report.testing.utils.generator.publication.SamplePublication;
import no.sikt.nva.data.report.testing.utils.model.EventConsumptionAttributes;
import no.sikt.nva.data.report.testing.utils.model.IndexDocument;
import no.sikt.nva.data.report.testing.utils.model.NviIndexDocument;
import no.sikt.nva.data.report.testing.utils.model.PublicationIndexDocument;
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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class CsvTransformerTest {

    public static final String PERSISTED_RESOURCES_NVI_CANDIDATES = "nvi-candidates";
    public static final String PERSISTED_RESOURCES_PUBLICATIONS = "resources";
    private static final Environment environment = new Environment();
    private S3Driver s3KeyBatches3Driver;
    private ByteArrayOutputStream outputStream;
    private S3Driver s3OutputDriver;
    private S3Driver s3ResourcesDriver;
    private EventBridgeClient eventBridgeClient;
    private CsvTransformer handler;
    private S3Client s3keyBatchClient;
    private S3Client s3ResourcesClient;

    public static EventConsumptionAttributes randomConsumptionAttribute() {
        return new EventConsumptionAttributes(PERSISTED_RESOURCES_PUBLICATIONS, SortableIdentifier.next().toString());
    }

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        s3keyBatchClient = new FakeS3Client();
        s3KeyBatches3Driver = new S3Driver(s3keyBatchClient, environment.readEnv("KEY_BATCHES_BUCKET"));
        var s3OutputClient = new FakeS3Client();
        s3OutputDriver = new S3Driver(s3OutputClient, environment.readEnv("EXPORT_BUCKET"));
        s3ResourcesClient = new FakeS3Client();
        s3ResourcesDriver = new S3Driver(s3ResourcesClient, environment.readEnv("EXPANDED_RESOURCES_BUCKET"));
        eventBridgeClient = new StubEventBridgeClient();
        handler = new CsvTransformer(s3keyBatchClient, s3ResourcesClient, s3OutputClient, eventBridgeClient);
    }

    @ParameterizedTest
    @EnumSource(names = {"AFFILIATION", "CONTRIBUTOR", "FUNDING", "IDENTIFIER", "PUBLICATION"})
    void shouldWriteCsvFileToS3ForAllReportTypes(ReportType reportType) throws IOException {
        var testData = new SampleData(generateDatePairs(1));
        var batch = setupExistingBatch(testData, reportType);
        var location = PERSISTED_RESOURCES_PUBLICATIONS;
        var batchKey = UnixPath.of(location).addChild(randomString());
        s3KeyBatches3Driver.insertFile(batchKey, batch);
        handler.handleRequest(eventStream(null, location), outputStream, mock(Context.class));
        var actualContent = attempt(() -> sortResponse(CSV, getActualPersistedFile(reportType), PUBLICATION_ID,
                                                       CONTRIBUTOR_IDENTIFIER)).orElseThrow();
        var expectedContent = getExpectedResponseData(reportType, testData);
        assertEquals(expectedContent, actualContent);
    }

    @ParameterizedTest
    @EnumSource(names = {"AFFILIATION", "CONTRIBUTOR", "FUNDING", "IDENTIFIER", "PUBLICATION"})
    void shouldWriteCsvFilesForAllReportTypesToSpecificFolderInExportBucket(ReportType reportType) throws IOException {
        var testData = new SampleData(generateDatePairs(1));
        var batch = setupExistingBatch(testData, reportType);
        var location = PERSISTED_RESOURCES_PUBLICATIONS;
        var batchKey = UnixPath.of(location).addChild(randomString());
        s3KeyBatches3Driver.insertFile(batchKey, batch);
        handler.handleRequest(eventStream(null, location), outputStream, mock(Context.class));
        var expectedPath = UnixPath.of(reportType.getType());
        var file = s3OutputDriver.listAllFiles(expectedPath).getFirst();
        assertNotNull(file);
    }

    @Test
    void shouldWriteCsvFileToS3ForReportTypeNvi() throws IOException {
        var reportType = ReportType.NVI;
        var testData = new SampleData(generateDatePairs(2));
        var batch = setupExistingBatch(testData, reportType);
        var location = PERSISTED_RESOURCES_NVI_CANDIDATES;
        var batchKey = UnixPath.of(location).addChild(randomString());
        s3KeyBatches3Driver.insertFile(batchKey, batch);
        handler.handleRequest(eventStream(null, location), outputStream, mock(Context.class));
        var actualContent = attempt(() -> sortResponse(CSV, getActualPersistedFile(reportType), PUBLICATION_ID,
                                                       CONTRIBUTOR_IDENTIFIER)).orElseThrow();
        var expectedContent = getExpectedResponseData(reportType, testData);
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void shouldWriteFilesWithCsvFileExtension() throws IOException {
        var batch = setupExistingBatch(new SampleData(generateDatePairs(1)), ReportType.PUBLICATION);
        var location = PERSISTED_RESOURCES_PUBLICATIONS;
        s3KeyBatches3Driver.insertFile(UnixPath.of(location).addChild(randomString()), batch);
        handler.handleRequest(eventStream(null, location), outputStream, mock(Context.class));
        var file = s3OutputDriver.listAllFiles(UnixPath.ROOT_PATH).getFirst();
        assertTrue(file.getLastPathElement().contains(".csv"));
    }

    @Test
    void shouldWriteFilesWithContentTypeAndEncoding() throws IOException {
        var batch = setupExistingBatch(new SampleData(generateDatePairs(1)), ReportType.PUBLICATION);
        var location = PERSISTED_RESOURCES_PUBLICATIONS;
        s3KeyBatches3Driver.insertFile(UnixPath.of(location).addChild(randomString()), batch);
        var mockedS3OutputClient = mock(S3Client.class);
        var handler = new CsvTransformer(s3keyBatchClient, s3ResourcesClient, mockedS3OutputClient, eventBridgeClient);
        handler.handleRequest(eventStream(null, location), outputStream, mock(Context.class));
        var requestWithExpectedContentType = PutObjectRequest.builder()
                                                 .contentType("text/csv; charset=UTF-8")
                                                 .contentEncoding("UTF-8")
                                                 .build();
        verify(mockedS3OutputClient, times(5))
            .putObject(refEq(requestWithExpectedContentType, "key", "bucket"), any(RequestBody.class));
    }

    @Test
    void shouldEncodeCsvFileInUtf8() throws IOException {
        var testData = new SampleData(generateDatePairs(1));
        var batch = setupExistingBatch(testData, ReportType.PUBLICATION);
        var reportType = ReportType.PUBLICATION;
        var location = PERSISTED_RESOURCES_PUBLICATIONS;
        s3KeyBatches3Driver.insertFile(UnixPath.of(location).addChild(randomString()), batch);
        handler.handleRequest(eventStream(null, location), outputStream, mock(Context.class));
        var expectedEncoding = StandardCharsets.UTF_8;
        var actualContent = s3OutputDriver.getUncompressedFile(getFirstFilePath(reportType), expectedEncoding);
        var expectedContent = getExpectedResponseData(reportType, testData);
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void shouldNotEmitNewEventWhenNoMoreBatchesToRetrieve() throws IOException {
        var testData = new SampleData(generateDatePairs(2));
        var batch = setupExistingBatch(testData, ReportType.PUBLICATION);
        var location = PERSISTED_RESOURCES_PUBLICATIONS;
        var batchKey = UnixPath.of(location).addChild(randomString());
        s3KeyBatches3Driver.insertFile(batchKey, batch);
        handler.handleRequest(eventStream(null, location), outputStream, mock(Context.class));
        var emittedEvent = ((StubEventBridgeClient) eventBridgeClient).getLatestEvent();
        assertNull(emittedEvent);
    }

    @Test
    void shouldEmitNewEventWhenThereAreMoreBatchesToProcess() throws IOException {
        var testData = new SampleData(generateDatePairs(2));
        var batch = setupExistingBatch(testData, ReportType.PUBLICATION);
        var location = PERSISTED_RESOURCES_PUBLICATIONS;
        var batchKey = UnixPath.of(location).addChild(randomString());
        s3KeyBatches3Driver.insertFile(batchKey, batch);
        var expectedStarMarkerFromEmittedEvent = UnixPath.of(location).addChild(randomString());
        s3KeyBatches3Driver.insertFile(expectedStarMarkerFromEmittedEvent, batch);
        var list = new ArrayList<String>();
        list.add(null);
        list.add(batchKey.toString());
        list.add(expectedStarMarkerFromEmittedEvent.toString());
        for (var item : list) {
            handler.handleRequest(eventStream(item, location), outputStream, mock(Context.class));

            var emittedEvent = ((StubEventBridgeClient) eventBridgeClient).getLatestEvent();

            assertEquals(batchKey.toString(), emittedEvent.getStartMarker());
        }
    }

    @Test
    void shouldNotFailWhenBlobNotFound() throws IOException {
        var testData = new SampleData(generateDatePairs(2));
        var indexDocuments = createAndPersistIndexDocuments(testData, ReportType.PUBLICATION);
        removeOneResourceFromPersistedResourcesBucket(indexDocuments);
        var batch = indexDocuments.stream()
                        .map(IndexDocument::getIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var location = PERSISTED_RESOURCES_PUBLICATIONS;
        var batchKey = UnixPath.of(location).addChild(randomString());
        s3KeyBatches3Driver.insertFile(batchKey, batch);
        assertDoesNotThrow(
            () -> handler.handleRequest(eventStream(null, location), outputStream,
                                        mock(Context.class)));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUnknownBatchLocationProvided() throws IOException {
        var location = "unknown";
        setUpValidTestData(location);
        assertThrows(IllegalArgumentException.class,
                     () -> handler.handleRequest(eventStream(null, location), outputStream, mock(Context.class)));
    }

    @Test
    void shouldSkipEmptyBatches() throws IOException {
        var location = PERSISTED_RESOURCES_NVI_CANDIDATES;
        var batchKey = UnixPath.of(location).addChild(randomString());
        s3KeyBatches3Driver.insertFile(batchKey, StringUtils.EMPTY_STRING);
        handler.handleRequest(eventStream(null, location), outputStream, mock(Context.class));

        var actual = s3OutputDriver.listAllFiles(UnixPath.of(""));
        assertEquals(0, actual.size());
    }

    List<DatePair> generateDatePairs(int numberOfDatePairs) {
        return IntStream.range(0, numberOfDatePairs)
                   .mapToObj(i -> new DatePair(new PublicationDate("2024", "02", "02"),
                                               Instant.now().minus(100, ChronoUnit.DAYS)))
                   .toList();
    }

    private static String getExpectedResponseData(ReportType reportType, SampleData test) {
        return switch (reportType) {
            case AFFILIATION -> test.getAffiliationResponseData();
            case CONTRIBUTOR -> test.getContributorResponseData();
            case FUNDING -> test.getFundingResponseData();
            case IDENTIFIER -> test.getIdentifierResponseData();
            case PUBLICATION -> test.getPublicationResponseData();
            case NVI -> test.getNviResponseData();
        };
    }

    private static IndexDocument toIndexDocument(SamplePublication publication) {
        return new IndexDocument(randomConsumptionAttribute(), PublicationIndexDocument.from(publication).asJsonNode());
    }

    private static IndexDocument toIndexDocument(SampleNviCandidate nviCandidate) {
        return new IndexDocument(randomConsumptionAttribute(), NviIndexDocument.from(nviCandidate).asJsonNode());
    }

    private UnixPath getFirstFilePath(ReportType reportType) {
        return s3OutputDriver.listAllFiles(UnixPath.of(reportType.getType())).getFirst();
    }

    private void setUpValidTestData(String location) throws IOException {
        var testData = new SampleData(generateDatePairs(2));
        var batch = setupExistingBatch(testData, ReportType.PUBLICATION);
        var batchKey = UnixPath.of(location).addChild(randomString());
        s3KeyBatches3Driver.insertFile(batchKey, batch);
    }

    private String setupExistingBatch(SampleData sampleData, ReportType type) {
        var indexDocuments = createAndPersistIndexDocuments(sampleData, type);
        return indexDocuments.stream()
                   .map(IndexDocument::getIdentifier)
                   .collect(Collectors.joining(System.lineSeparator()));
    }

    private void removeOneResourceFromPersistedResourcesBucket(List<IndexDocument> expectedDocuments) {
        var document = expectedDocuments.getFirst();
        s3ResourcesDriver.deleteFile(UnixPath.of(document.getIdentifier()));
    }

    private List<IndexDocument> createAndPersistIndexDocuments(SampleData sampleData, ReportType type) {
        var indexDocuments = ReportType.NVI.equals(type)
                                 ? createAndPersistNviData(sampleData)
                                 : createAndPersistPublications(sampleData);
        indexDocuments.forEach(document -> document.persistInS3(s3ResourcesDriver));
        return indexDocuments;
    }

    private List<IndexDocument> createAndPersistPublications(SampleData sampleData) {
        return sampleData.getPublicationTestData().stream()
                   .map(CsvTransformerTest::toIndexDocument)
                   .toList();
    }

    private List<IndexDocument> createAndPersistNviData(SampleData sampleData) {
        return sampleData.getNviTestData().stream()
                   .map(CsvTransformerTest::toIndexDocument)
                   .toList();
    }

    private String getActualPersistedFile(ReportType reportType) {
        var file = getFirstFilePath(reportType);
        return s3OutputDriver.getFile(file);
    }

    private InputStream eventStream(String startMarker, String location) throws JsonProcessingException {
        var event = new AwsEventBridgeEvent<KeyBatchRequestEvent>();
        event.setDetail(new KeyBatchRequestEvent(startMarker, randomString(), location));
        event.setId(randomString());
        var jsonString = dtoObjectMapper.writeValueAsString(event);
        return IoUtils.stringToStream(jsonString);
    }
}
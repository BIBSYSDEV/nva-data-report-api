package no.sikt.nva.data.report.api.etl;

import static commons.model.ReportType.AFFILIATION;
import static commons.model.ReportType.CONTRIBUTOR;
import static commons.model.ReportType.FUNDING;
import static commons.model.ReportType.IDENTIFIER;
import static commons.model.ReportType.NVI;
import static commons.model.ReportType.PUBLICATION;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import commons.model.ReportType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import no.sikt.nva.data.report.api.etl.aws.S3StorageReader;
import no.sikt.nva.data.report.api.etl.aws.S3StorageWriter;
import no.sikt.nva.data.report.api.etl.model.EventType;
import no.sikt.nva.data.report.api.etl.model.PersistedResourceEvent;
import no.sikt.nva.data.report.testing.utils.generator.SampleData;
import no.sikt.nva.data.report.testing.utils.model.IndexDocument;
import no.sikt.nva.data.report.testing.utils.model.NviIndexDocument;
import no.sikt.nva.data.report.testing.utils.model.PublicationIndexDocument;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class PersistedResourceCsvTransformerTest {

    public static final String EXPORT_BUCKET = "exportBucket";
    private static final String GZIP_ENDING = ".gz";
    private static final String UPSERT_EVENT = EventType.UPSERT.getValue();
    private static final String RESOURCES_PATH = "resources";
    private static final String BUCKET_NAME = "notRelevant";
    private static Context context;
    private static PersistedResourceCsvTransformer handler;
    private static S3Driver s3ResourcesDriver;
    private S3Driver s3OutputDriver;
    private S3Client fakeS3ResourcesClient;

    @BeforeEach
    void setup() {
        context = new FakeContext();
        fakeS3ResourcesClient = new FakeS3Client();
        s3ResourcesDriver = new S3Driver(fakeS3ResourcesClient, BUCKET_NAME);
        var fakeS3OutputClient = new FakeS3Client();
        s3OutputDriver = new S3Driver(fakeS3OutputClient, EXPORT_BUCKET);
        handler = new PersistedResourceCsvTransformer(new S3StorageReader(fakeS3ResourcesClient, BUCKET_NAME),
                                                      new S3StorageWriter(fakeS3OutputClient, EXPORT_BUCKET));
    }

    @Test
    void shouldFetchNviIndexDocumentAndTransformToCsvInExportBucket() throws IOException {
        var testData = new SampleData();
        var event = setupExistingNviIndexDocumentAndCreateUpsertEvent(testData);
        handler.handleRequest(event, context);
        var expected = testData.getNviResponseData();
        var actual = getActualPersistedFile(NVI);
        assertEqualsInAnyOrder(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(names = {"AFFILIATION", "CONTRIBUTOR", "FUNDING", "IDENTIFIER", "PUBLICATION"})
    void shouldFetchPublicationIndexDocumentAndTransformToKnownReportTypeAsCsvInExportBucket(ReportType reportType)
        throws IOException {
        var testData = new SampleData();
        var event = setUpExistingPublicationIndexDocumentAndCreateUpsertEvent(testData);
        handler.handleRequest(event, context);
        var expected = getExpectedData(testData, reportType);
        var actual = getActualPersistedFile(reportType);
        assertEqualsInAnyOrder(expected, actual);
    }

    @Test
    void shouldNotExportHandleIdentifiers() throws IOException {
        var testData = new SampleData();
        var event = setUpExistingPublicationIndexDocumentAndCreateUpsertEvent(testData);
        handler.handleRequest(event, context);
        var expected = testData.getIdentifierResponseData();
        var actual = getActualPersistedFile(IDENTIFIER);
        assertEqualsInAnyOrder(expected, actual);
    }

    @Test
    void shouldFetchPublicationIndexDocumentAndTransformToExpectedNumberOfFiles()
        throws IOException {
        var event = setUpExistingPublicationIndexDocumentAndCreateUpsertEvent(new SampleData());
        handler.handleRequest(event, context);
        var expectedReportTypes = List.of(PUBLICATION, AFFILIATION, CONTRIBUTOR, FUNDING, IDENTIFIER);
        var expectedNumberOfFiles = expectedReportTypes.size();
        assertEquals(expectedNumberOfFiles, s3OutputDriver.listAllFiles(UnixPath.ROOT_PATH).size());
        expectedReportTypes.forEach(
            reportType -> assertEquals(1, s3OutputDriver.listAllFiles(UnixPath.of(reportType.getType())).size()));
    }

    @Test
    void fileNameShouldContainReportTypeAndIdentifierAndTimeStamp() throws IOException {
        var nviCandidate = new SampleData().getNviTestData().getFirst();
        var indexDocument = IndexDocument.from(NviIndexDocument.from(nviCandidate));
        var objectKey = setupExistingObjectInS3(indexDocument);
        var event = createUpsertEvent(objectKey);
        handler.handleRequest(event, context);
        var reportType = NVI.getType();
        var actualPath = getFirstWithParent(reportType);
        var expectedPathWithIdentifier = UnixPath.of(reportType).addChild(indexDocument.getIdentifier()).toString();
        assertTrue(actualPath.toString().contains(expectedPathWithIdentifier));
        assertTrue(actualPath.toString().contains(LocalDate.now().toString()));
        assertTrue(actualPath.getLastPathElement().contains(".csv"));
    }

    @Test
    void shouldWriteFilesWithContentTypeAndEncoding() throws IOException {
        var event = setupExistingNviIndexDocumentAndCreateUpsertEvent(new SampleData());
        var mockedS3OutputClient = mock(S3Client.class);
        var handler = new PersistedResourceCsvTransformer(new S3StorageReader(fakeS3ResourcesClient, BUCKET_NAME),
                                                          new S3StorageWriter(mockedS3OutputClient, EXPORT_BUCKET));
        handler.handleRequest(event, context);
        var requestWithExpectedContentType = PutObjectRequest.builder()
                                                 .contentType("text/csv; charset=UTF-8")
                                                 .contentEncoding("UTF-8")
                                                 .build();
        verify(mockedS3OutputClient, times(1))
            .putObject(refEq(requestWithExpectedContentType, "key", "bucket"), any(RequestBody.class));
    }

    @Test
    void shouldEncodeCsvFileInUtf8() throws IOException {
        var testData = new SampleData();
        var event = setupExistingNviIndexDocumentAndCreateUpsertEvent(testData);
        handler.handleRequest(event, context);
        var expectedEncoding = StandardCharsets.UTF_8;
        var actualContent = s3OutputDriver.getUncompressedFile(getFirstWithParent(NVI.getType()),
                                                               expectedEncoding);
        var expectedContent = testData.getNviResponseData();
        assertEquals(expectedContent, actualContent);
    }

    @ParameterizedTest
    @ValueSource(strings = {"someKeyWithOutParentFolder", ""})
    void shouldThrowIllegalArgumentExceptionWhenKeyIsInvalid(String key) {
        var event = createSqsEvent(new PersistedResourceEvent(BUCKET_NAME, key, UPSERT_EVENT));
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(event, context));
    }

    @ParameterizedTest
    @ValueSource(strings = {"someUnknownEventType", ""})
    void shouldThrowIllegalArgumentExceptionIfEventTypeIsUnknownOrBlank(String eventType) {
        var key = UnixPath.of(RESOURCES_PATH, randomString()).toString();
        var event = createSqsEvent(new PersistedResourceEvent(BUCKET_NAME, key, eventType));
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(event, context));
    }

    private static SQSEvent createSqsEvent(PersistedResourceEvent persistedResourceEvent) {
        var sqsEvent = new SQSEvent();
        var message = new SQSMessage();
        message.setBody(persistedResourceEvent.toJsonString());
        sqsEvent.setRecords(List.of(message));
        return sqsEvent;
    }

    private static SQSEvent createUpsertEvent(UnixPath objectKey) {
        return createSqsEvent(
            new PersistedResourceEvent(BUCKET_NAME, objectKey.toString(), EventType.UPSERT.getValue()));
    }

    private static String constructCompressedFileIdentifier(UUID identifier) {
        return identifier.toString() + GZIP_ENDING;
    }

    private void assertEqualsInAnyOrder(String expected, String actual) {
        var expectedLines = expected.split(System.lineSeparator());
        var actualLines = actual.split(System.lineSeparator());
        assertEquals(expectedLines.length, actualLines.length);
        var expectedList = List.of(expectedLines);
        var actualList = List.of(actualLines);
        assertTrue(expectedList.containsAll(actualList));
        assertTrue(actualList.containsAll(expectedList));
    }

    private String getExpectedData(SampleData sampleData, ReportType reportType) {
        return switch (reportType) {
            case AFFILIATION -> sampleData.getAffiliationResponseData();
            case CONTRIBUTOR -> sampleData.getContributorResponseData();
            case FUNDING -> sampleData.getFundingResponseData();
            case IDENTIFIER -> sampleData.getIdentifierResponseData();
            case PUBLICATION -> sampleData.getPublicationResponseData();
            case NVI -> sampleData.getNviResponseData();
        };
    }

    private SQSEvent setUpExistingPublicationIndexDocumentAndCreateUpsertEvent(SampleData sampleData)
        throws IOException {
        var publication = sampleData.getPublicationTestData().getFirst();
        var indexDocument = PublicationIndexDocument.from(publication).toIndexDocument();
        var objectKey = setupExistingObjectInS3(indexDocument);
        return createUpsertEvent(objectKey);
    }

    private SQSEvent setupExistingNviIndexDocumentAndCreateUpsertEvent(SampleData sampleData)
        throws IOException {
        var nviCandidate = sampleData.getNviTestData().getFirst();
        var indexDocument = NviIndexDocument.from(nviCandidate).toIndexDocument();
        var objectKey = setupExistingObjectInS3(indexDocument);
        return createUpsertEvent(objectKey);
    }

    private String getActualPersistedFile(ReportType reportType) {
        var file = getFirstWithParent(reportType.getType());
        return s3OutputDriver.getFile(file);
    }

    private UnixPath getFirstWithParent(String parent) {
        return s3OutputDriver.listAllFiles(UnixPath.of(parent)).getFirst();
    }

    private UnixPath setupExistingObjectInS3(IndexDocument document)
        throws IOException {
        var objectKey = UnixPath.of(document.getIndex(),
                                    constructCompressedFileIdentifier(UUID.fromString(document.getIdentifier())));
        s3ResourcesDriver.insertFile(objectKey, document.toJsonString());
        return objectKey;
    }
}

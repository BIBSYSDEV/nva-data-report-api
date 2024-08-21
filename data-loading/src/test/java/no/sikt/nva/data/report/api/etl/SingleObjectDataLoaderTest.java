package no.sikt.nva.data.report.api.etl;

import static no.sikt.nva.data.report.testing.utils.ResultSorter.sortResponse;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_ID;
import static no.sikt.nva.data.report.testing.utils.model.ResultType.CSV;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.Context;
import commons.model.ReportType;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;
import no.sikt.nva.data.report.api.etl.aws.S3StorageReader;
import no.sikt.nva.data.report.api.etl.aws.S3StorageWriter;
import no.sikt.nva.data.report.api.etl.model.EventType;
import no.sikt.nva.data.report.api.etl.model.PersistedResourceEvent;
import no.sikt.nva.data.report.testing.utils.generator.TestData;
import no.sikt.nva.data.report.testing.utils.model.IndexDocument;
import no.sikt.nva.data.report.testing.utils.model.NviIndexDocument;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SingleObjectDataLoaderTest {

    public static final String EXPORT_BUCKET = "exportBucket";
    private static final String GZIP_ENDING = ".gz";
    private static final String UPSERT_EVENT = EventType.UPSERT.getValue();
    private static final String RESOURCES_PATH = "resources";
    private static final String BUCKET_NAME = "notRelevant";
    private static Context context;
    private static SingleObjectDataLoader handler;
    private static S3Driver s3ResourcesDriver;
    private S3Driver s3OutputDriver;

    @BeforeEach
    void setup() {
        context = new FakeContext();
        var fakeS3ResourcesClient = new FakeS3Client();
        s3ResourcesDriver = new S3Driver(fakeS3ResourcesClient, BUCKET_NAME);
        var fakeS3OutputClient = new FakeS3Client();
        s3OutputDriver = new S3Driver(fakeS3OutputClient, EXPORT_BUCKET);
        handler = new SingleObjectDataLoader(new S3StorageReader(fakeS3ResourcesClient, BUCKET_NAME),
                                             new S3StorageWriter(fakeS3OutputClient, EXPORT_BUCKET));
    }

    @Test
    void shouldFetchNviIndexDocumentAndTransformToCsvInExportBucket() throws IOException {
        var testData = new TestData();
        var nviCandidate = testData.getNviTestData().getFirst();
        var indexDocument = IndexDocument.fromNviCandidate(NviIndexDocument.from(nviCandidate));
        var objectKey = setupExistingObjectInS3(indexDocument);
        var event = createUpsertEvent(objectKey);
        handler.handleRequest(event, context);
        var expected = testData.getNviResponseData();
        var actual = attempt(() -> sortResponse(CSV, getActualPersistedFile(ReportType.NVI), PUBLICATION_ID,
                                                CONTRIBUTOR_IDENTIFIER)).orElseThrow();
        assertEquals(expected, actual);
    }

    @Test
    void fileNameShouldContainReportTypeAndIdentifierAndTimeStamp() throws IOException {
        var nviCandidate = new TestData().getNviTestData().getFirst();
        var indexDocument = IndexDocument.fromNviCandidate(NviIndexDocument.from(nviCandidate));
        var objectKey = setupExistingObjectInS3(indexDocument);
        var event = createUpsertEvent(objectKey);
        handler.handleRequest(event, context);
        var reportType = ReportType.NVI.getType();
        var actualPath = getFirstWithParent(reportType).toString();
        var expectedPathWithIdentifier = UnixPath.of(reportType).addChild(indexDocument.getIdentifier()).toString();
        assertTrue(actualPath.contains(expectedPathWithIdentifier));
        assertTrue(actualPath.contains(LocalDate.now().toString()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"someKeyWithOutParentFolder", ""})
    void shouldThrowIllegalArgumentExceptionWhenKeyIsInvalid(String key) {
        var event = new PersistedResourceEvent(BUCKET_NAME, key, UPSERT_EVENT);
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(event, context));
    }

    @ParameterizedTest
    @ValueSource(strings = {"someUnknownEventType", ""})
    void shouldThrowIllegalArgumentExceptionIfEventTypeIsUnknownOrBlank(String eventType) {
        var key = UnixPath.of(RESOURCES_PATH, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, eventType);
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(event, context));
    }

    private static PersistedResourceEvent createUpsertEvent(UnixPath objectKey) {
        return new PersistedResourceEvent(BUCKET_NAME, objectKey.toString(), EventType.UPSERT.getValue());
    }

    private static String constructCompressedFileIdentifier(UUID identifier) {
        return identifier.toString() + GZIP_ENDING;
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

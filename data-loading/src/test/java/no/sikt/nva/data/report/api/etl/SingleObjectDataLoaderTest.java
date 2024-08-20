package no.sikt.nva.data.report.api.etl;

import static no.sikt.nva.data.report.testing.utils.ResultSorter.sortResponse;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_ID;
import static no.sikt.nva.data.report.testing.utils.model.ResultType.CSV;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.amazonaws.services.lambda.runtime.Context;
import commons.model.ReportType;
import java.io.IOException;
import java.util.UUID;
import no.sikt.nva.data.report.api.etl.model.EventType;
import no.sikt.nva.data.report.api.etl.model.PersistedResourceEvent;
import no.sikt.nva.data.report.api.etl.service.GraphService;
import no.sikt.nva.data.report.api.etl.service.S3StorageReader;
import no.sikt.nva.data.report.testing.utils.generator.TestData;
import no.sikt.nva.data.report.testing.utils.model.IndexDocument;
import no.sikt.nva.data.report.testing.utils.model.NviIndexDocument;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.Environment;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SingleObjectDataLoaderTest {

    private static final String GZIP_ENDING = ".gz";
    private static final String UPSERT_EVENT = EventType.UPSERT.getValue();
    private static final String RESOURCES_PATH = "resources";
    private static final String BUCKET_NAME = "notRelevant";
    private static Context context;
    private static SingleObjectDataLoader handler;
    private static S3Driver s3Driver;
    private S3Driver s3OutputDriver;

    @BeforeEach
    void setup() {
        context = new FakeContext();
        var fakeS3Client = new FakeS3Client();
        s3Driver = new S3Driver(fakeS3Client, BUCKET_NAME);
        var s3OutputClient = new FakeS3Client();
        s3OutputDriver = new S3Driver(s3OutputClient, new Environment().readEnv("EXPORT_BUCKET"));
        handler = new SingleObjectDataLoader(new GraphService(null), new S3StorageReader(fakeS3Client, BUCKET_NAME));
    }

    @Test
    void shouldFetchNviIndexDocumentAndTransformToCsvInExportBucket() throws IOException {
        TestData testData = new TestData();
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

    private static String constructFileIdentifier(UUID identifier) {
        return identifier.toString() + GZIP_ENDING;
    }

    private String getActualPersistedFile(ReportType reportType) {
        var file = s3OutputDriver.listAllFiles(UnixPath.of(reportType.getType())).getFirst();
        return s3OutputDriver.getFile(file);
    }

    private UnixPath setupExistingObjectInS3(IndexDocument document)
        throws IOException {
        var objectKey = UnixPath.of(document.getIndex(),
                                    constructFileIdentifier(UUID.fromString(document.getDocumentIdentifier())));
        s3Driver.insertFile(objectKey, document.toJsonString());
        return objectKey;
    }
}

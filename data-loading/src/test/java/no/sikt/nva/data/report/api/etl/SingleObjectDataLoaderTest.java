package no.sikt.nva.data.report.api.etl;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.Context;
import commons.db.DatabaseConnection;
import commons.db.GraphStoreProtocolConnection;
import java.io.IOException;
import java.util.UUID;
import no.sikt.nva.data.report.api.etl.model.EventType;
import no.sikt.nva.data.report.api.etl.model.PersistedResourceEvent;
import no.sikt.nva.data.report.api.etl.service.GraphService;
import no.sikt.nva.data.report.api.etl.service.S3StorageReader;
import no.sikt.nva.data.report.api.etl.testutils.model.nvi.IndexDocumentWithConsumptionAttributes;
import no.sikt.nva.data.report.api.etl.testutils.model.nvi.NviCandidateIndexDocument;
import no.unit.nva.s3.S3Driver;
import no.sikt.nva.data.report.testing.utils.FusekiTestingServer;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.Environment;
import nva.commons.core.paths.UnixPath;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.TestFormatter;

class SingleObjectDataLoaderTest {

    private static final String GZIP_ENDING = ".gz";
    private static final String UPSERT_EVENT = EventType.UPSERT.getValue();
    private static final String NVI_CONTEXT = "https://api.dev.nva.aws.unit.no/scientific-index/context";
    private static final String GSP_ENDPOINT = "/gsp";
    private static final String NVI_CANDIDATES_FOLDER = "nvi-candidates";
    private static final String RESOURCES_FOLDER = "resources";
    private static final String BUCKET_NAME = "notRelevant";
    private static final String HOST = "example.org";
    private static Context context;
    private static SingleObjectDataLoader handler;
    private static FusekiServer server;
    private static DatabaseConnection dbConnection;
    private static S3Driver s3Driver;
    private static S3StorageReader storageReader;

    @BeforeAll
    static void setup() {
        context = new FakeContext();
        var dataSet = DatasetFactory.createTxnMem();
        server = FusekiTestingServer.init(dataSet, GSP_ENDPOINT);
        var url = server.serverURL();
        var queryPath = new Environment().readEnv("QUERY_PATH");
        dbConnection = new GraphStoreProtocolConnection(url, url, queryPath);
        var fakeS3Client = new FakeS3Client();
        s3Driver = new S3Driver(fakeS3Client, BUCKET_NAME);
        storageReader = new S3StorageReader(fakeS3Client, BUCKET_NAME);
        handler = new SingleObjectDataLoader(new GraphService(dbConnection), storageReader);
    }

    @AfterAll
    static void tearDown() {
        server.stop();
    }

    @AfterEach
    void clearDatabase() {
        dbConnection.delete();
    }

    //TODO: Test storing in named graph
    @Test
    void shouldFetchNviCandidateFromBucketAndStoreInGraph() throws IOException {
        var candidateDocument = IndexDocumentWithConsumptionAttributes.from(randomCandidate());
        var objectKey = UnixPath.of(NVI_CANDIDATES_FOLDER,
                                    constructFileIdentifier(candidateDocument.consumptionAttributes()
                                                                .documentIdentifier()));
        s3Driver.insertFile(objectKey, candidateDocument.toJsonString());
        var event = new PersistedResourceEvent(BUCKET_NAME, objectKey.toString(), EventType.UPSERT.getValue());
        handler.handleRequest(event, context);
        var query = QueryFactory.create("SELECT * WHERE { ?a ?b ?c }");
        var result = dbConnection.getResult(query, new TestFormatter());
        //TODO: Create expected triple and compare with result
        assertTrue(result.contains(candidateDocument.indexDocument().identifier().toString()));
    }

    @Test
    void shouldLogSuccessfulDatabaseConnection() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        new SingleObjectDataLoader(new GraphService(dbConnection), storageReader);
        assertTrue(logAppender.getMessages().contains("Connection"));
    }

    @Test
    void shouldLogStuff() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        new SingleObjectDataLoader(new GraphService(dbConnection), storageReader);
        assertTrue(logAppender.getMessages().contains("Initializing SingleObjectDataLoader"));
    }

    @ParameterizedTest(name = "Should extract and log folderName {0}")
    @ValueSource(strings = {RESOURCES_FOLDER, NVI_CANDIDATES_FOLDER})
    void shouldExtractAndLogObjectKey(String folderName) throws IOException {
        var objectKey = setupExistingObjectInS3(folderName);
        var event = new PersistedResourceEvent(BUCKET_NAME, objectKey.toString(), UPSERT_EVENT);
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("object key: " + objectKey));
    }

    @ParameterizedTest
    @ValueSource(strings = {"someKeyWithOutParentFolder", ""})
    void shouldThrowIllegalArgumentExceptionWhenKeyIsInvalid(String key) {
        var event = new PersistedResourceEvent(BUCKET_NAME, key, UPSERT_EVENT);
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(event, context));
    }

    @ParameterizedTest(name = "Should extract and log eventType type {0}")
    @ValueSource(strings = {"PutObject", "DeleteObject"})
    void shouldExtractAndLogOperationType(String eventType) throws IOException {
        var objectKey = setupExistingObjectInS3(NVI_CANDIDATES_FOLDER);
        var event = new PersistedResourceEvent(BUCKET_NAME, objectKey.toString(), eventType);
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("eventType: " + eventType));
    }

    @ParameterizedTest
    @ValueSource(strings = {"someUnknownEventType", ""})
    void shouldThrowIllegalArgumentExceptionIfEventTypeIsUnknownOrBlank(String eventType) {
        var key = UnixPath.of(RESOURCES_FOLDER, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, eventType);
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(event, context));
    }

    private static String constructFileIdentifier(UUID identifier) {
        return identifier.toString() + GZIP_ENDING;
    }

    private static void initializeGraphServer(Dataset dataSet) {
        server = FusekiServer.create()
                     .add(GSP_ENDPOINT, dataSet)
                     .build();
        server.start(); // Initialise server before using it!
    }

    private UnixPath setupExistingObjectInS3(String folder) throws IOException {
        var candidateDocument = IndexDocumentWithConsumptionAttributes.from(randomCandidate());
        var objectKey = UnixPath.of(folder,
                                    constructFileIdentifier(candidateDocument.consumptionAttributes()
                                                                .documentIdentifier()));
        s3Driver.insertFile(objectKey, candidateDocument.toJsonString());
        return objectKey;
    }

    //TODO: Add more data to NviCandidateIndexDocument
    private NviCandidateIndexDocument randomCandidate() {
        var identifier = UUID.randomUUID();
        return NviCandidateIndexDocument.builder()
                   .withId(UriWrapper.fromHost(HOST).addChild(identifier.toString()).getUri())
                   .withContext(NVI_CONTEXT)
                   .withIdentifier(identifier)
                   .build();
    }
}

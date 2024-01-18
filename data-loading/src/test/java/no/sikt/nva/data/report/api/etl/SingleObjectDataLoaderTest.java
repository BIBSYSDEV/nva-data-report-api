package no.sikt.nva.data.report.api.etl;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.Context;
import commons.db.GraphStoreProtocolConnection;
import java.io.IOException;
import java.util.UUID;
import no.sikt.nva.data.report.api.etl.model.EventType;
import no.sikt.nva.data.report.api.etl.model.PersistedResourceEvent;
import no.sikt.nva.data.report.api.etl.service.GraphService;
import no.sikt.nva.data.report.api.etl.testutils.model.nvi.IndexDocumentWithConsumptionAttributes;
import no.sikt.nva.data.report.api.etl.testutils.model.nvi.NviCandidateIndexDocument;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.Environment;
import nva.commons.core.paths.UnixPath;
import nva.commons.logutils.LogUtils;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test.TestFormatter;

class SingleObjectDataLoaderTest {

    public static final String NVI_CONTEXT = "https://api.dev.nva.aws.unit.no/scientific-index/context";
    public static final String GZIP_ENDING = ".gz";
    private static final String GSP_ENDPOINT = "/gsp";
    private static final String NVI_CANDIDATES_FOLDER = "nvi-candidates";
    private static final String RESOURCES_FOLDER = "resources";
    private static final String SOME_OPERATION = "someOperation";
    private static final String BUCKET_NAME = "notRelevant";
    private Context context;
    private SingleObjectDataLoader handler;
    private FusekiServer server;
    private GraphStoreProtocolConnection dbConnection;
    private S3Driver s3Driver;

    @BeforeEach
    void setup() {
        context = new FakeContext();
        var dataSet = DatasetFactory.createTxnMem();
        initializeGraphServer(dataSet);
        var url = server.serverURL();
        var queryPath = new Environment().readEnv("QUERY_PATH");
        dbConnection = new GraphStoreProtocolConnection(url, url, queryPath);
        handler = new SingleObjectDataLoader(new GraphService(dbConnection));
        var s3Client = new FakeS3Client();
        s3Driver = new S3Driver(s3Client, BUCKET_NAME);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldFetchNviCandidateFromBucketAndStoreInGraph() throws IOException {
        var candidateDocument = IndexDocumentWithConsumptionAttributes.from(randomCandidate());
        var key = UnixPath.of(NVI_CANDIDATES_FOLDER, constructFileIdentifier(candidateDocument.consumptionAttributes()
                                                                                 .documentIdentifier()));
        s3Driver.insertFile(key, candidateDocument.toJsonString());
        var event = new PersistedResourceEvent(BUCKET_NAME, key.toString(), EventType.UPSERT.getValue());
        handler.handleRequest(event, context);
        var query = QueryFactory.create("SELECT * WHERE { ?a ?b ?c }");
        var result = dbConnection.getResult(query, new TestFormatter());
        assertTrue(result.contains(candidateDocument.indexDocument().identifier().toString()));
    }

    @Test
    void shouldLogSuccessfulDatabaseConnection() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        new SingleObjectDataLoader(new GraphService(dbConnection));
        assertTrue(logAppender.getMessages().contains("Connection"));
    }

    @Test
    void shouldLogStuff() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        new SingleObjectDataLoader(new GraphService(dbConnection));
        assertTrue(logAppender.getMessages().contains("Initializing SingleObjectDataLoader"));
    }

    @ParameterizedTest(name = "Should extract and log folderName {0}")
    @ValueSource(strings = {RESOURCES_FOLDER, NVI_CANDIDATES_FOLDER})
    void shouldExtractAndLogObjectParentFolder(String folderName) {
        var key = UnixPath.of(folderName, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, SOME_OPERATION);
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("object folder: " + folderName));
    }

    @Test
    void shouldLogNoFolderIfKeyHasNoParent() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        var key = randomString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, SOME_OPERATION);
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("No parent folder"));
    }

    @ParameterizedTest(name = "Should extract and log eventType type {0}")
    @ValueSource(strings = {"PutObject", "DeleteObject"})
    void shouldExtractAndLogOperationType(String eventType) {
        var key = UnixPath.of(RESOURCES_FOLDER, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, eventType);
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("eventType: " + eventType));
    }

    @Test
    void shouldLogUnknownEventTypeIfEventTypeIsUnknown() {
        var key = UnixPath.of(RESOURCES_FOLDER, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, randomString());
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("Unknown event type: " + event.eventType()));
    }

    private static String constructFileIdentifier(UUID identifier) {
        return identifier.toString() + GZIP_ENDING;
    }

    private NviCandidateIndexDocument randomCandidate() {
        return NviCandidateIndexDocument.builder()
                   .withContext(NVI_CONTEXT)
                   .withIdentifier(UUID.randomUUID())
                   .build();
    }

    private void initializeGraphServer(Dataset dataSet) {
        server = FusekiServer.create()
                     .add(GSP_ENDPOINT, dataSet)
                     .build();
        server.start(); // Initialise server before using it!
    }
}

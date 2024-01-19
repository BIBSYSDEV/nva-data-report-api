package no.sikt.nva.data.report.api.etl;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.Context;
import commons.db.DatabaseConnection;
import commons.db.GraphStoreProtocolConnection;
import no.sikt.nva.data.report.api.etl.model.EventType;
import no.sikt.nva.data.report.api.etl.model.PersistedResourceEvent;
import no.sikt.nva.data.report.api.etl.service.GraphService;
import no.sikt.nva.data.report.testing.utils.FusekiTestingServer;
import no.unit.nva.stubs.FakeContext;
import nva.commons.core.Environment;
import nva.commons.core.paths.UnixPath;
import nva.commons.logutils.LogUtils;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SingleObjectDataLoaderTest {

    private static final String GSP_ENDPOINT = "/gsp";
    private static final String NVI_CANDIDATES_FOLDER = "nvi-candidates";
    private static final String RESOURCES_FOLDER = "resources";
    private static final String SOME_OPERATION = "someOperation";
    private static final String BUCKET_NAME = "notRelevant";
    private static Context context;
    private static SingleObjectDataLoader handler;
    private static FusekiServer server;
    private static DatabaseConnection dbConnection;

    @BeforeAll
    static void setup() {
        context = new FakeContext();
        var dataSet = DatasetFactory.createTxnMem();
        server = FusekiTestingServer.init(dataSet, GSP_ENDPOINT);
        var url = server.serverURL();
        var queryPath = new Environment().readEnv("QUERY_PATH");
        dbConnection = new GraphStoreProtocolConnection(url, url, queryPath);
        handler = new SingleObjectDataLoader(new GraphService(dbConnection));
    }

    @AfterAll
    static void tearDown() {
        server.stop();
    }

    @AfterEach
    void clearDatabase() {
        dbConnection.delete();
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
        assertTrue(logAppender.getMessages().contains("Initializing DataLoader"));
    }

    @ParameterizedTest(name = "Should extract and log folderName {0}")
    @ValueSource(strings = {RESOURCES_FOLDER, NVI_CANDIDATES_FOLDER})
    void shouldExtractAndLogObjectParentFolder(String folderName) {
        var key = UnixPath.of(folderName, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, SOME_OPERATION);
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("Object folder: " + folderName));
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
        assertTrue(logAppender.getMessages().contains("Event type: " + EventType.parse(eventType)));
    }

    @Test
    void shouldLogUnknownEventTypeIfEventTypeIsUnknown() {
        var key = UnixPath.of(RESOURCES_FOLDER, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, randomString());
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("Unknown event type: " + event.eventType()));
    }
}

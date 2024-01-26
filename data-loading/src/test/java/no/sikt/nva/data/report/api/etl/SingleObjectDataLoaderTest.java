package no.sikt.nva.data.report.api.etl;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.Context;
import commons.db.DatabaseConnection;
import commons.db.GraphStoreProtocolConnection;
import commons.db.utils.GraphName;

import java.net.URI;
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
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import no.sikt.nva.data.report.testing.utils.TestFormatter;

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

    private URI graph;

    @BeforeAll
    static void setup() {
        context = new FakeContext();
        var dataSet = DatasetFactory.createTxnMem();
        server = FusekiTestingServer.init(dataSet, GSP_ENDPOINT);
        var url = server.serverURL();
        var queryPath = new Environment().readEnv("QUERY_PATH");
        dbConnection = new GraphStoreProtocolConnection(url, queryPath);
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
        try {
            dbConnection.delete(graph);
        } catch (Exception e) {
            // Necessary to avoid case where we hve already deleted the graph
            catchExpectedExceptionsExceptHttpException(e);
        }
    }

    //TODO: Test storing in named graph
    @Test
    void shouldFetchNviCandidateFromBucketAndStoreInGraph() throws IOException {
        var candidateDocument = IndexDocumentWithConsumptionAttributes.from(randomCandidate());
        var objectKey = UnixPath.of(NVI_CANDIDATES_FOLDER,
                                    constructFileIdentifier(candidateDocument
                                                                .consumptionAttributes()
                                                                .documentIdentifier()));
        registerGraphForPostTestDeletion(objectKey);
        s3Driver.insertFile(objectKey, candidateDocument.toJsonString());
        var event = new PersistedResourceEvent(BUCKET_NAME,
                                               objectKey.toString(),
                                               EventType.UPSERT.getValue());
        handler.handleRequest(event, context);
        var query = QueryFactory.create("SELECT * WHERE { GRAPH ?g { ?a ?b ?c } }");
        var result = dbConnection.getResult(query, new TestFormatter());
        //TODO: Create expected triple and compare with result
        assertTrue(result.contains(candidateDocument.indexDocument().identifier().toString()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://example.org/defaults-to-nva",
        "https://api.dev.nva.aws.unit.no/publication/context"
    })
    void shouldReplaceContextWhenJsonLdIsConsumed(String contextUri) throws IOException {
        var json = """
            { "body": {
                "@context": "__REPLACE__",
                "id": "https://example.org/a",
                "type": "ExampleData",
                "labels": { "en": "Example data" }
              }
            }
            """.replace("__REPLACE__", contextUri);
        var identifier = UUID.randomUUID();
        var objectKey = UnixPath.of(NVI_CANDIDATES_FOLDER,
                                    constructFileIdentifier(identifier));
        registerGraphForPostTestDeletion(objectKey);
        s3Driver.insertFile(objectKey, json);
        var event = new PersistedResourceEvent(BUCKET_NAME,
                                               objectKey.toString(),
                                               EventType.UPSERT.getValue());
        handler.handleRequest(event, context);
        var query = QueryFactory.create("SELECT * WHERE { GRAPH ?g { ?a ?b ?c } }");
        var result = dbConnection.getResult(query, new TestFormatter());
        var expected = String.format("<https://example.org/a> "
                                     + "<https://nva.sikt.no/ontology/publication#label> "
                                     + "\"Example data\"@en "
                                     + "<https://example.org/nvi-candidates/%s.nt> ."
                                     + System.lineSeparator()
                                     + "<https://example.org/a> "
                                     + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "
                                     + "<https://nva.sikt.no/ontology/publication#ExampleData> "
                                     + "<https://example.org/nvi-candidates/%s.nt> .",
                                     identifier, identifier);
        assertEquals(expected, result);
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
        registerGraphForPostTestDeletion(objectKey);
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
        registerGraphForPostTestDeletion(objectKey);
        var event = new PersistedResourceEvent(BUCKET_NAME, objectKey.toString(), eventType);
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("eventType: " + eventType));
    }

    /**
     * This method adds the named graph URI in the deletion pool for post-test removal. This is
     * necessary since we add the graph to a database and potentially return multiple graphs
     * if a query is too broad. For example, where the SPARQL query has GRAPH ?g rather than
     * a specific named graph.
     */
    private void registerGraphForPostTestDeletion(UnixPath objectKey) {
        graph = GraphName.newBuilder()
                   .withBase("example.org")
                   .fromUnixPath(objectKey)
                   .build()
                   .toUri();
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

    private UnixPath setupExistingObjectInS3(String folder) throws IOException {
        var candidateDocument = IndexDocumentWithConsumptionAttributes.from(randomCandidate());
        var objectKey = UnixPath.of(folder,
                                    constructFileIdentifier(candidateDocument
                                                                .consumptionAttributes()
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

    private static void catchExpectedExceptionsExceptHttpException(Exception e) {
        if (!(e instanceof HttpException)) {
            throw new RuntimeException(e);
        }
    }
}

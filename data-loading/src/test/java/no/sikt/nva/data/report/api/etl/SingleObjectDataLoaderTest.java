package no.sikt.nva.data.report.api.etl;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.Context;
import commons.db.DatabaseConnection;
import commons.db.GraphStoreProtocolConnection;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import no.sikt.nva.data.report.api.etl.model.EventType;
import no.sikt.nva.data.report.api.etl.model.PersistedResourceEvent;
import no.sikt.nva.data.report.api.etl.service.GraphService;
import no.sikt.nva.data.report.api.etl.service.S3StorageReader;
import no.sikt.nva.data.report.api.etl.testutils.model.nvi.IndexDocument;
import no.sikt.nva.data.report.api.etl.testutils.model.nvi.IndexDocumentWithConsumptionAttributes;
import no.sikt.nva.data.report.testing.utils.FusekiTestingServer;
import no.sikt.nva.data.report.testing.utils.TestFormatter;
import no.unit.nva.s3.S3Driver;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SingleObjectDataLoaderTest {

    private static final String GZIP_ENDING = ".gz";
    private static final String UPSERT_EVENT = EventType.UPSERT.getValue();
    private static final String NVI_CONTEXT = "https://api.dev.nva.aws.unit.no/scientific-index/context";
    private static final String GSP_ENDPOINT = "/gsp";
    private static final String NVI_PATH = "nvi-candidates";
    private static final String RESOURCES_PATH = "resources";
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

    @Test
    void shouldFetchResourceFromBucketAndStoreInNamedGraph() throws IOException {
        var indexDocument = IndexDocumentWithConsumptionAttributes.from(randomIndexDocument());
        var objectKey = constructObjectKey(indexDocument);
        var expectedNamedGraph = registerGraphForPostTestDeletion(indexDocument.indexDocument().id());
        s3Driver.insertFile(objectKey, indexDocument.toJsonString());
        var event = createUpsertEvent(objectKey);
        handler.handleRequest(event, context);
        var identifier = indexDocument.indexDocument().identifier().toString();
        var expectedTriple = String.format("<" + UriWrapper.fromHost(HOST)
                                                     .addChild(NVI_PATH)
                                                     .addChild(identifier).toString() + "> "
                                           + "<https://nva.sikt.no/ontology/publication#identifier> "
                                           + "\"" + identifier + "\"" + " .");
        var result = dbConnection.fetch(expectedNamedGraph);
        assertTrue(result.contains(expectedTriple));
    }

    @Test
    void shouldUpdateNamedGraphWithNewDataOnPutEvent() throws IOException {
        var indexDocument = IndexDocumentWithConsumptionAttributes.from(randomIndexDocument());
        var expectedNamedGraph = setupExistingResourceInGraph(indexDocument);
        var updatedIndexDocument = IndexDocumentWithConsumptionAttributes.from(
            indexDocument.indexDocument().copy().withSomeProperty("someUpdatedValue").build());
        var objectKey = constructObjectKey(updatedIndexDocument);
        s3Driver.insertFile(objectKey, updatedIndexDocument.toJsonString());
        handler.handleRequest(createUpsertEvent(objectKey), context);
        var result = dbConnection.fetch(expectedNamedGraph);
        var expectedUpdatedPropertyTriple = generatePropertyTriple(updatedIndexDocument);
        assertTrue(result.contains(expectedUpdatedPropertyTriple));
        var oldPropertyValue = generatePropertyTriple(indexDocument);
        assertFalse(result.contains(oldPropertyValue));
    }

    @ParameterizedTest
    @DisplayName("Should replace remote context with inline context")
    @ValueSource(strings = {
        "https://example.org/defaults-to-nva",
        "https://api.dev.nva.aws.unit.no/publication/context"
    })
    void shouldReplaceContextWhenJsonLdIsConsumed(String contextUri) throws IOException {
        var identifier = UUID.randomUUID();
        var objectKey = UnixPath.of(NVI_PATH,
                                    constructFileIdentifier(identifier));
        var uri = URI.create("https://example.org/"
                             + objectKey.toString().replace(".gz", ""));
        var json = String.format("""
            { "body": {
                "@context": "%s",
                "id": "%s",
                "type": "ExampleData",
                "labels": { "en": "Example data" }
              }
            }
            """,  contextUri, uri);

        var graphUri = registerGraphForPostTestDeletion(uri);
        s3Driver.insertFile(objectKey, json);
        var event = createUpsertEvent(objectKey);
        handler.handleRequest(event, context);
        var query = QueryFactory.create("SELECT * WHERE { GRAPH ?g { ?a ?b ?c } }");
        var result = dbConnection.getResult(query, new TestFormatter());
        var expected = "<" + uri + "> "
                       + "<https://nva.sikt.no/ontology/publication#label> "
                       + "\"Example data\"@en "
                       + "<" + graphUri + "> ."
                       + System.lineSeparator()
                       + "<" + uri + "> "
                       + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "
                       + "<https://nva.sikt.no/ontology/publication#ExampleData> "
                       + "<" + graphUri + "> .";
        assertEquals(expected, result);
    }

    @Test
    void shouldLogStuff() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        new SingleObjectDataLoader(new GraphService(dbConnection), storageReader);
        assertTrue(logAppender.getMessages().contains("Initializing SingleObjectDataLoader"));
    }

    // Possibly
    @ParameterizedTest(name = "Should extract and log folderName {0}")
    @ValueSource(strings = {RESOURCES_PATH, NVI_PATH})
    void shouldExtractAndLogObjectKey(String folderName) throws IOException {
        var document = IndexDocumentWithConsumptionAttributes.from(randomIndexDocument());
        var objectKey = setupExistingObjectInS3(folderName, document);
        registerGraphForPostTestDeletion(document.indexDocument().id());
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
        var document = IndexDocumentWithConsumptionAttributes.from(randomIndexDocument());
        var objectKey = setupExistingObjectInS3(NVI_PATH, document);
        registerGraphForPostTestDeletion(document.indexDocument().id());
        var event = new PersistedResourceEvent(BUCKET_NAME, objectKey.toString(), eventType);
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("eventType: " + eventType));
    }

    @ParameterizedTest
    @ValueSource(strings = {"someUnknownEventType", ""})
    void shouldThrowIllegalArgumentExceptionIfEventTypeIsUnknownOrBlank(String eventType) {
        var key = UnixPath.of(RESOURCES_PATH, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, eventType);
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(event, context));
    }

    private static UnixPath constructObjectKey(IndexDocumentWithConsumptionAttributes updatedIndexDocument) {
        return UnixPath.of(NVI_PATH,
                           constructFileIdentifier(updatedIndexDocument
                                                       .consumptionAttributes()
                                                       .documentIdentifier()));
    }

    private static PersistedResourceEvent createUpsertEvent(UnixPath objectKey) {
        return new PersistedResourceEvent(BUCKET_NAME,
                                          objectKey.toString(),
                                          EventType.UPSERT.getValue());
    }

    private static String constructFileIdentifier(UUID identifier) {
        return identifier.toString() + GZIP_ENDING;
    }

    private static void catchExpectedExceptionsExceptHttpException(Exception e) {
        if (!(e instanceof HttpException)) {
            throw new RuntimeException(e);
        }
    }

    private String generatePropertyTriple(IndexDocumentWithConsumptionAttributes document) {
        return String.format("<" + UriWrapper.fromHost(HOST)
                                       .addChild(NVI_PATH)
                                       .addChild(document
                                                     .indexDocument()
                                                     .identifier()
                                                     .toString())
                                       .toString() + "> "
                             + "<https://nva.sikt.no/ontology/publication#someProperty> "
                             + "\"" + document.indexDocument()
                                          .someProperty() + "\"" + " .");
    }

    private URI setupExistingResourceInGraph(IndexDocumentWithConsumptionAttributes candidateDocument)
        throws IOException {
        var objectKey = constructObjectKey(candidateDocument);
        registerGraphForPostTestDeletion(candidateDocument.indexDocument().id());
        s3Driver.insertFile(objectKey, candidateDocument.toJsonString());
        var event = createUpsertEvent(objectKey);
        handler.handleRequest(event, context);
        return graph;
    }

    /**
     * This method adds the named graph URI in the deletion pool for post-test removal. This is necessary since we add
     * the graph to a database and potentially return multiple graphs if a query is too broad. For example, where the
     * SPARQL query has GRAPH ?g rather than a specific named graph.
     */
    private URI registerGraphForPostTestDeletion(URI uri) {
        graph = URI.create(uri + ".nt");
        return graph;
    }

    private UnixPath setupExistingObjectInS3(String folder,
                                             IndexDocumentWithConsumptionAttributes document)
        throws IOException {
        var objectKey = UnixPath.of(folder, constructFileIdentifier(document
                                                                        .consumptionAttributes()
                                                                        .documentIdentifier()));
        s3Driver.insertFile(objectKey, document.toJsonString());
        return objectKey;
    }

    private IndexDocument randomIndexDocument() {
        var identifier = UUID.randomUUID();
        return IndexDocument.builder()
                   .withId(UriWrapper
                               .fromHost(HOST)
                               .addChild(NVI_PATH)
                               .addChild(identifier.toString())
                               .getUri())
                   .withContext(NVI_CONTEXT)
                   .withIdentifier(identifier)
                   .withSomeProperty(randomString())
                   .build();
    }
}

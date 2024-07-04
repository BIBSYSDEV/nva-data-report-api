package no.sikt.nva.data.report.api.etl;

import static no.sikt.nva.data.report.testing.utils.ViewCompilerTestUtils.getNviCandidateJsonNode;
import static no.sikt.nva.data.report.testing.utils.ViewCompilerTestUtils.getPublicationJsonNode;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commons.ViewCompiler;
import commons.db.DatabaseConnection;
import commons.db.GraphStoreProtocolConnection;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.UUID;
import no.sikt.nva.data.report.api.etl.model.EventType;
import no.sikt.nva.data.report.api.etl.model.PersistedResourceEvent;
import no.sikt.nva.data.report.api.etl.service.GraphService;
import no.sikt.nva.data.report.api.etl.service.S3StorageReader;
import no.sikt.nva.data.report.api.etl.testutils.model.nvi.IndexDocumentWithConsumptionAttributes;
import no.sikt.nva.data.report.testing.utils.FusekiTestingServer;
import no.sikt.nva.data.report.testing.utils.TestFormatter;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
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
        try {
            dbConnection.delete(graph);
        } catch (Exception e) {
            // Necessary to avoid case where we hve already deleted the graph
            catchExpectedExceptionsExceptHttpException(e);
        }
    }

    @Test
    void shouldFetchResourceFromBucketAndStoreInNamedGraphWithAppliedView() throws IOException {
        var identifier = UUID.randomUUID();
        var id = getUri(identifier);
        var documentBody = getNviCandidateJsonNode(id);
        var indexDocument =
            IndexDocumentWithConsumptionAttributes.from(documentBody, identifier);
        var objectKey = constructObjectKey(indexDocument);
        var expectedNamedGraph = registerGraphForPostTestDeletion(id);
        s3Driver.insertFile(objectKey, indexDocument.toJsonString());
        var event = createUpsertEvent(objectKey);
        handler.handleRequest(event, context);
        var expected = new ViewCompiler(IoUtils.stringToStream(documentBody.toString())).extractView();
        var result = dbConnection.fetch(expectedNamedGraph);
        var actualModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actualModel, IoUtils.stringToStream(result), Lang.NTRIPLES);
        assertTrue(expected.isIsomorphicWith(actualModel));
    }

    @ParameterizedTest
    @DisplayName("Should replace remote context with inline context")
    @ValueSource(strings = {
        "https://example.org/defaults-to-nva",
        "https://api.dev.nva.aws.unit.no/publication/context"
    })
    void shouldReplaceContextWhenJsonLdIsConsumed(String contextUri) throws IOException {
        var identifier = UUID.randomUUID();
        var objectKey = UnixPath.of(RESOURCES_PATH, constructFileIdentifier(identifier));
        var uri = URI.create("https://example.org/"
                             + objectKey.toString().replace(".gz", ""));
        var graphUri = registerGraphForPostTestDeletion(uri);
        var json = getDocumentWithRemoteContext(contextUri, uri, identifier);
        s3Driver.insertFile(objectKey, json);
        var event = createUpsertEvent(objectKey);
        handler.handleRequest(event, context);
        var query = QueryFactory.create("SELECT * WHERE { GRAPH ?g { ?a ?b ?c } }");
        var result = dbConnection.getResult(query, new TestFormatter());
        var expected = "<" + uri + "> "
                       + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "
                       + "<https://nva.sikt.no/ontology/publication#AcademicArticle> "
                       + "<" + graphUri + "> .";
        assertTrue(result.contains(expected));
    }

    @Test
    void shouldUpdateNamedGraphWithNewDataOnPutEvent() throws IOException {
        var identifier = UUID.randomUUID();
        var id = getUri(identifier);
        var documentBody = getNviCandidateJsonNode(id);
        var indexDocument = IndexDocumentWithConsumptionAttributes.from(documentBody, identifier);
        setupExistingObjectInS3(NVI_PATH, indexDocument);
        var expectedNamedGraph = setupExistingResourceInGraph(id, indexDocument);
        var updatedDocument = updateProperty(documentBody);
        var updatedIndexDocument = IndexDocumentWithConsumptionAttributes.from(updatedDocument, identifier);
        var objectKey = constructObjectKey(updatedIndexDocument);
        s3Driver.insertFile(objectKey, updatedIndexDocument.toJsonString());
        handler.handleRequest(createUpsertEvent(objectKey), context);
        var expected = new ViewCompiler(IoUtils.stringToStream(updatedDocument.toString())).extractView();
        var result = dbConnection.fetch(expectedNamedGraph);
        var actualModel = ModelFactory.createDefaultModel();
        RDFDataMgr.read(actualModel, IoUtils.stringToStream(result), Lang.NTRIPLES);
        assertTrue(expected.isIsomorphicWith(actualModel));
    }

    @Test
    void shouldLogStuff() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        new SingleObjectDataLoader(new GraphService(dbConnection), storageReader);
        logAppender.stop();
        assertTrue(logAppender.getMessages().contains("Initializing SingleObjectDataLoader"));
    }

    // Possibly
    @ParameterizedTest(name = "Should extract and log folderName {0}")
    @ValueSource(strings = {RESOURCES_PATH, NVI_PATH})
    void shouldExtractAndLogObjectKey(String folderName) throws IOException {
        var identifier = UUID.randomUUID();
        var id = getUri(identifier);
        var document = IndexDocumentWithConsumptionAttributes.from(getNviCandidateJsonNode(id), identifier);
        var objectKey = setupExistingObjectInS3(folderName, document);
        registerGraphForPostTestDeletion(id);
        var event = new PersistedResourceEvent(BUCKET_NAME, objectKey.toString(), UPSERT_EVENT);
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        logAppender.stop();
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
        var identifier = UUID.randomUUID();
        var id = getUri(identifier);
        var document = IndexDocumentWithConsumptionAttributes.from(getNviCandidateJsonNode(id), identifier);
        var objectKey = setupExistingObjectInS3(NVI_PATH, document);
        registerGraphForPostTestDeletion(id);
        var event = new PersistedResourceEvent(BUCKET_NAME, objectKey.toString(), eventType);
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        logAppender.stop();
        assertTrue(logAppender.getMessages().contains("eventType: " + eventType));
    }

    @ParameterizedTest
    @ValueSource(strings = {"someUnknownEventType", ""})
    void shouldThrowIllegalArgumentExceptionIfEventTypeIsUnknownOrBlank(String eventType) {
        var key = UnixPath.of(RESOURCES_PATH, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, eventType);
        assertThrows(IllegalArgumentException.class, () -> handler.handleRequest(event, context));
    }

    private static String getDocumentWithRemoteContext(String contextUri, URI uri, UUID identifier) {
        var jsonWithRemoteContext = (ObjectNode) getPublicationJsonNode(uri);
        jsonWithRemoteContext.put("@context", contextUri);
        return IndexDocumentWithConsumptionAttributes.from(jsonWithRemoteContext, identifier).toJsonString();
    }

    private static URI getUri(UUID identifier) {
        return UriWrapper.fromHost(HOST)
                   .addChild(NVI_PATH)
                   .addChild(identifier.toString()).getUri();
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

    private String toTriples(Model model) {
        var stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, model, org.apache.jena.riot.Lang.NTRIPLES);
        return stringWriter.toString();
    }

    private JsonNode updateProperty(JsonNode documentBody) {
        var updatedDocument = documentBody.deepCopy();
        ((ObjectNode) updatedDocument).put("isApplicable", false);
        return updatedDocument;
    }

    private URI setupExistingResourceInGraph(URI id, IndexDocumentWithConsumptionAttributes candidateDocument)
        throws IOException {
        var objectKey = constructObjectKey(candidateDocument);
        registerGraphForPostTestDeletion(id);
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
}

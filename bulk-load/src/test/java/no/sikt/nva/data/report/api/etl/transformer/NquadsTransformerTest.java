package no.sikt.nva.data.report.api.etl.transformer;

import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.ViewCompiler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.sikt.nva.data.report.testing.utils.StaticTestDataUtil;
import no.sikt.nva.data.report.testing.utils.model.EventConsumptionAttributes;
import no.sikt.nva.data.report.testing.utils.model.IndexDocument;
import no.sikt.nva.data.report.testing.utils.stubs.StubEventBridgeClient;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.StringUtils;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import nva.commons.logutils.LogUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.s3.S3Client;

class NquadsTransformerTest {

    private static final String DEFAULT_LOCATION = "resources";
    private static final ObjectMapper objectMapperWithEmpty = JsonUtils.dtoObjectMapper;
    private ByteArrayOutputStream outputStream;
    private S3Client s3ResourcesClient;
    private S3Driver s3ResourcesDriver;
    private S3Client s3BatchesClient;
    private S3Driver s3BatchesDriver;
    private EventBridgeClient eventBridgeClient;
    private S3Client s3OutputClient;
    private S3Driver s3OutputDriver;

    @BeforeEach
    void setup() {
        outputStream = new ByteArrayOutputStream();
        s3ResourcesClient = new FakeS3Client();
        s3ResourcesDriver = new S3Driver(s3ResourcesClient, "resources");
        s3BatchesClient = new FakeS3Client();
        s3BatchesDriver = new S3Driver(s3BatchesClient, "batchesBucket");
        s3OutputClient = new FakeS3Client();
        s3OutputDriver = new S3Driver(s3OutputClient, "loaderBucket");

        eventBridgeClient = new StubEventBridgeClient();
    }

    @Test
    void shouldWriteNquadsToS3() throws IOException {
        var expectedDocuments = createExpectedDocuments(10);
        var batch = expectedDocuments.stream()
                        .map(IndexDocument::getDocumentIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var batchKey = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(batchKey), batch);
        var handler = new NquadsTransformer(s3ResourcesClient,
                                            s3BatchesClient,
                                            s3OutputClient,
                                            eventBridgeClient);
        handler.handleRequest(eventStream(null), outputStream, mock(Context.class));
        var contentString = getActualPersistedFile();
        var expectedModel = getExpectedModelWithAppliedView(expectedDocuments);
        var actualModel = getActualModel(contentString);
        assertTrue(expectedModel.isIsomorphicWith(actualModel));
    }

    @Test
    void shouldReplaceNullCharacters() throws IOException {
        var json = """
            {
                "body": {
                  "type": "Publication",
                  "@context": "https://api.dev.nva.aws.unit.no/publication/context",
                  "id": "https://example.org/publication/publicationIdentifier",
                  "identifier": "publicationIdentifier",
                  "entityDescription": {
                    "mainTitle": "\\u0000 propertyValue"
                  }
                }
              }
            """;
        var documentIdentifier = "publicationIdentifier";
        s3ResourcesDriver.insertFile(UnixPath.of(documentIdentifier), json);
        var batchKey = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(batchKey), documentIdentifier);
        var handler = new NquadsTransformer(s3ResourcesClient,
                                            s3BatchesClient,
                                            s3OutputClient,
                                            eventBridgeClient);
        handler.handleRequest(eventStream(null), outputStream, mock(Context.class));
        var contentString = getActualPersistedFile();
        assertFalse(contentString.contains("\\u0000"));
    }

    @Test
    void shouldSkipEmptyBatches() throws IOException {
        var batchKey = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(batchKey), StringUtils.EMPTY_STRING);
        var handler = new NquadsTransformer(s3ResourcesClient,
                                            s3BatchesClient,
                                            s3OutputClient,
                                            eventBridgeClient);
        handler.handleRequest(eventStream(null), outputStream, Mockito.mock(Context.class));

        var actual = s3OutputDriver.listAllFiles(UnixPath.of(""));
        assertEquals(0, actual.size());
    }

    @Test
    void shouldNotEmitNewEventWhenNoMoreBatchesToRetrieve() throws IOException {
        var expectedDocuments = createExpectedDocuments(10);
        var batch = expectedDocuments.stream()
                        .map(IndexDocument::getDocumentIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var batchKey = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(batchKey), batch);
        var handler = new NquadsTransformer(s3ResourcesClient,
                                            s3BatchesClient,
                                            s3OutputClient,
                                            eventBridgeClient);
        handler.handleRequest(eventStream(null), outputStream, Mockito.mock(Context.class));

        var emittedEvent = ((StubEventBridgeClient) eventBridgeClient).getLatestEvent();
        assertNull(emittedEvent);
    }

    @Test
    void shouldEmitNewEventWhenThereAreMoreBatchesToIndex() throws IOException {
        var expectedDocuments = createExpectedDocuments(10);
        var batch = expectedDocuments.stream()
                        .map(IndexDocument::getDocumentIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var batchKey = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(batchKey), batch);
        var expectedStarMarkerFromEmittedEvent = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(expectedStarMarkerFromEmittedEvent), batch);
        var list = new ArrayList<String>();
        list.add(null);
        list.add(batchKey);
        list.add(expectedStarMarkerFromEmittedEvent);
        var handler = new NquadsTransformer(s3ResourcesClient,
                                            s3BatchesClient,
                                            s3OutputClient,
                                            eventBridgeClient);
        for (var item : list) {
            handler.handleRequest(eventStream(item), outputStream, Mockito.mock(Context.class));

            var emittedEvent = ((StubEventBridgeClient) eventBridgeClient).getLatestEvent();

            assertEquals(batchKey, emittedEvent.getStartMarker());
            assertEquals(DEFAULT_LOCATION, emittedEvent.getLocation());
        }
    }

    // TODO: Remove test once we have figured out how the GraphName should be provided.
    @Test
    void throwsExceptionWhenEventBlobIsInvalid() {
        var invalid = """
            {
              "consumptionAttributes": {},
              "body": {}
            }
            """;
        var indexDocument = IndexDocument.fromJsonString(invalid);

        var exception = assertThrows(RuntimeException.class, indexDocument::getDocumentIdentifier);
        assertEquals("Missing identifier in resource", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInputJsonIsNonsense() throws IOException {
        final var loggerAppender = LogUtils.getTestingAppenderForRootLogger();
        var documents = new ArrayList<>(createExpectedDocuments(1));
        var node = objectMapper.createObjectNode()
                       .set("no", objectMapper.createObjectNode());
        var invalidDocument = new IndexDocument(randomConsumptionAttribute(), node);
        documents.add(invalidDocument);
        insertResourceInPersistedResourcesBucket(invalidDocument);
        var batch = documents.stream()
                        .map(IndexDocument::getDocumentIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var batchKey = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(batchKey), batch);
        var handler = new NquadsTransformer(s3ResourcesClient,
                                            s3BatchesClient,
                                            s3OutputClient,
                                            eventBridgeClient);
        Executable executable = () -> handler.handleRequest(eventStream(null),
                                                            outputStream,
                                                            mock(Context.class));
        assertThrows(MissingIdException.class, executable);
        assertTrue(loggerAppender.getMessages().contains("Missing id-node in content"));
    }

    @Test
    void shouldNotFailWhenBlobNotFound() throws IOException {
        var expectedDocuments = createExpectedDocuments(10);
        removeOneResourceFromPersistedResourcesBucket(expectedDocuments);
        var batch = expectedDocuments.stream()
                        .map(IndexDocument::getDocumentIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var batchKey = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(batchKey), batch);
        var handler = new NquadsTransformer(s3ResourcesClient,
                                            s3BatchesClient,
                                            s3OutputClient,
                                            eventBridgeClient);
        assertDoesNotThrow(() -> handler.handleRequest(eventStream(null), outputStream, Mockito.mock(Context.class)));
    }

    private static Model getActualModel(String nqauds) {
        var actualGraph = DatasetGraphFactory.createTxnMem();
        RDFDataMgr.read(actualGraph, IoUtils.stringToStream(nqauds), Lang.NQUADS);
        var actualModel = ModelFactory.createDefaultModel();
        actualGraph.stream()
            .map(Quad::getGraph)
            .map(actualGraph::getGraph)
            .map(ModelFactory::createModelForGraph)
            .forEach(actualModel::add);

        return actualModel;
    }

    private static Model getExpectedModelWithAppliedView(List<IndexDocument> expectedDocuments) {
        var expectedModel = ModelFactory.createDefaultModel();
        expectedDocuments.forEach(document -> applyView(document, expectedModel));
        return expectedModel;
    }

    private static void applyView(IndexDocument document, Model expectedModel) {
        var model = new ViewCompiler(IoUtils.stringToStream(document.getResource().toString()))
                        .extractView(document.getResourceId());
        expectedModel.add(model);
    }

    private static EventConsumptionAttributes randomConsumptionAttribute() {
        return new EventConsumptionAttributes(DEFAULT_LOCATION, SortableIdentifier.next());
    }

    private void removeOneResourceFromPersistedResourcesBucket(List<IndexDocument> expectedDocuments) {
        var document = expectedDocuments.getFirst();
        s3ResourcesDriver.deleteFile(UnixPath.of(document.getDocumentIdentifier()));
    }

    private String getActualPersistedFile() {
        var file = s3OutputDriver.listAllFiles(UnixPath.ROOT_PATH).getFirst();
        return s3OutputDriver.getFile(file);
    }

    private boolean modelHasData(String contentString) {
        var graph = DatasetGraphFactory.createTxnMem();
        RDFDataMgr.read(graph, IoUtils.stringToStream(contentString), Lang.NQUADS);
        return !graph.isEmpty();
    }

    private InputStream eventStream(String startMarker) throws JsonProcessingException {
        var event = new AwsEventBridgeEvent<KeyBatchRequestEvent>();
        event.setDetail(new KeyBatchRequestEvent(startMarker, randomString(), DEFAULT_LOCATION));
        event.setId(randomString());
        var jsonString = objectMapperWithEmpty.writeValueAsString(event);
        return IoUtils.stringToStream(jsonString);
    }

    private List<IndexDocument> createExpectedDocuments(int numberOfDocuments) {
        return IntStream.range(0, numberOfDocuments)
                   .mapToObj(i -> new IndexDocument(randomConsumptionAttribute(),
                                                    StaticTestDataUtil.getPublicationJsonNode(randomUri())))
                   .map(this::insertResourceInPersistedResourcesBucket)
                   .toList();
    }

    private IndexDocument insertResourceInPersistedResourcesBucket(IndexDocument document) {
        attempt(() -> s3ResourcesDriver.insertFile(UnixPath.of(document.getDocumentIdentifier()),
                                                   document.toJsonString())).orElseThrow();
        return document;
    }
}

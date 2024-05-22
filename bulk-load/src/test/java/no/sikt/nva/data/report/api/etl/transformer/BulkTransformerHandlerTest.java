package no.sikt.nva.data.report.api.etl.transformer;

import static java.util.UUID.randomUUID;
import static no.sikt.nva.data.report.testing.utils.QueueServiceTestUtils.emptyEvent;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.sikt.nva.data.report.api.etl.transformer.model.EventConsumptionAttributes;
import no.sikt.nva.data.report.api.etl.transformer.model.IndexDocument;
import no.sikt.nva.data.report.testing.utils.QueueServiceTestUtils;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.StringUtils;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import nva.commons.logutils.LogUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class BulkTransformerHandlerTest {

    private static final String IDENTIFIER = "__IDENTIFIER__";
    private static final String VALID_PUBLICATION = IoUtils.stringFromResources(Path.of("publication.json"));
    private static final String DEFAULT_LOCATION = "resources";
    private static final ObjectMapper objectMapperWithEmpty = dtoObjectMapper;
    private S3Driver s3ResourcesDriver;
    private S3Driver s3BatchesDriver;
    private S3Driver s3OutputDriver;
    private FakeSqsClient queueClient;
    private BulkTransformerHandler handler;
    private Context context;

    @BeforeEach
    void setup() {
        var s3ResourcesClient = new FakeS3Client();
        s3ResourcesDriver = new S3Driver(s3ResourcesClient, "resources");
        var s3BatchesClient = new FakeS3Client();
        s3BatchesDriver = new S3Driver(s3BatchesClient, "batchesBucket");
        var s3OutputClient = new FakeS3Client();
        s3OutputDriver = new S3Driver(s3OutputClient, "loaderBucket");
        queueClient = new FakeSqsClient();
        handler = new BulkTransformerHandler(s3ResourcesClient, s3BatchesClient, s3OutputClient, queueClient);
        context = mock(Context.class);
    }

    @AfterEach
    void tearDown() {
        queueClient.removeSentMessages();
    }

    @Test
    void shouldWriteNquadsToS3() throws IOException {
        var expectedDocuments = createExpectedDocuments(10);
        var batch = expectedDocuments.stream()
                        .map(IndexDocument::getDocumentIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var batchKey = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(batchKey), batch);
        handler.handleRequest(sqsEvent(null), context);
        var file = s3OutputDriver.listAllFiles(UnixPath.of("")).getFirst();
        var contentString = s3OutputDriver.getFile(file);
        assertTrue(modelHasData(contentString));
    }

    @Test
    void shouldSkipEmptyBatches() throws IOException {
        var batchKey = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(batchKey), StringUtils.EMPTY_STRING);
        handler.handleRequest(sqsEvent(null), context);

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
        handler.handleRequest(sqsEvent(null), context);
        assertEquals(0, queueClient.getSentMessages().size());
    }

    @Test
    void shouldEmitNewEventWhenThereAreMoreBatchesToIndex() throws IOException {
        var firstBatch = getBatch(10);
        var firstBatchKey = "firstBatchKey";
        s3BatchesDriver.insertFile(UnixPath.of(firstBatchKey), firstBatch);
        var secondBatchKey = "secondBatchKey";
        var secondBatch = getBatch(10);
        s3BatchesDriver.insertFile(UnixPath.of(secondBatchKey), secondBatch);
        var thirdBatchKey = "thirdBatchKey";
        var thirdBatch = getBatch(10);
        s3BatchesDriver.insertFile(UnixPath.of(thirdBatchKey), thirdBatch);

        handler.handleRequest(sqsEvent(null), context);
        var firstContinuationToken = getKeyBatchRequestEvent(0).getContinuationToken();
        handler.handleRequest(sqsEvent(firstBatchKey), context);
        var secondContinuationToken = getKeyBatchRequestEvent(1).getContinuationToken();
        assertNotEquals(firstContinuationToken, secondContinuationToken);
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
    void shouldProcessWithDefaultsWhenInputEventIsNull() throws IOException {
        s3BatchesDriver.insertFile(UnixPath.of("someKey"), getBatch(10));
        assertDoesNotThrow(() -> handler.handleRequest(emptyEvent(), mock(Context.class)));
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
        Executable executable = () -> handler.handleRequest(sqsEvent(null), mock(Context.class));
        assertThrows(MissingIdException.class, executable);
        assertTrue(loggerAppender.getMessages().contains("Missing id-node in content"));
    }

    private static EventConsumptionAttributes randomConsumptionAttribute() {
        return new EventConsumptionAttributes(DEFAULT_LOCATION, SortableIdentifier.next());
    }

    private KeyBatchRequestEvent getKeyBatchRequestEvent(int index) {
        return KeyBatchRequestEvent.fromJsonString(queueClient.getSentMessages().get(index).messageBody());
    }

    private String getBatch(int numberOfDocuments) {
        return createExpectedDocuments(numberOfDocuments).stream()
                   .map(IndexDocument::getDocumentIdentifier)
                   .collect(Collectors.joining(System.lineSeparator()));
    }

    private boolean modelHasData(String contentString) {
        var graph = DatasetGraphFactory.createTxnMem();
        RDFDataMgr.read(graph, IoUtils.stringToStream(contentString), Lang.NQUADS);
        return !graph.isEmpty();
    }

    private SQSEvent sqsEvent(String continuationToken) {
        var event = new KeyBatchRequestEvent(continuationToken, DEFAULT_LOCATION);
        return QueueServiceTestUtils.createEvent(event.toJsonString());
    }

    private List<IndexDocument> createExpectedDocuments(int numberOfDocuments) {
        return IntStream.range(0, numberOfDocuments)
                   .mapToObj(i -> new IndexDocument(randomConsumptionAttribute(),
                                                    randomValidNode()))
                   .map(this::insertResourceInPersistedResourcesBucket)
                   .toList();
    }

    private IndexDocument insertResourceInPersistedResourcesBucket(IndexDocument document) {
        attempt(() -> s3ResourcesDriver.insertFile(UnixPath.of(document.getDocumentIdentifier()),
                                                   document.toJsonString())).orElseThrow();
        return document;
    }

    private JsonNode randomValidNode() {
        return attempt(
            () -> objectMapperWithEmpty.readTree(
                VALID_PUBLICATION.replace(IDENTIFIER, randomUUID().toString()))).orElseThrow();
    }
}

package no.sikt.nva.data.report.api.etl.transformer;

import static java.util.UUID.randomUUID;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.sikt.nva.data.report.api.etl.transformer.model.EventConsumptionAttributes;
import no.sikt.nva.data.report.api.etl.transformer.model.IndexDocument;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.SingletonCollector;
import nva.commons.core.StringUtils;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import nva.commons.logutils.LogUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

class BulkTransformerHandlerTest {

    private static final String IDENTIFIER = "__IDENTIFIER__";
    private static final String VALID_PUBLICATION = IoUtils.stringFromResources(Path.of("publication.json"));
    private static final String DEFAULT_LOCATION = "resources";
    private static final ObjectMapper objectMapperWithEmpty = JsonUtils.dtoObjectMapper;
    private ByteArrayOutputStream outputStream;
    private S3Driver s3ResourcesDriver;
    private S3Driver s3BatchesDriver;
    private EventBridgeClient eventBridgeClient;
    private S3Driver s3OutputDriver;
    private BulkTransformerHandler handler;

    @BeforeEach
    void setup() {
        outputStream = new ByteArrayOutputStream();
        var s3ResourcesClient = new FakeS3Client();
        s3ResourcesDriver = new S3Driver(s3ResourcesClient, "resources");
        var s3BatchesClient = new FakeS3Client();
        s3BatchesDriver = new S3Driver(s3BatchesClient, "batchesBucket");
        var s3OutputClient = new FakeS3Client();
        s3OutputDriver = new S3Driver(s3OutputClient, "loaderBucket");
        eventBridgeClient = new StubEventBridgeClient();
        handler = new BulkTransformerHandler(s3ResourcesClient, s3BatchesClient, s3OutputClient, eventBridgeClient);
    }

    @Test
    void shouldWriteNquadsToS3() throws IOException {
        var expectedDocuments = createExpectedDocuments(10);
        var batch = expectedDocuments.stream()
                        .map(IndexDocument::getDocumentIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var batchKey = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(batchKey), batch);
        handler.handleRequest(eventStream(null), outputStream, mock(Context.class));
        var file = s3OutputDriver.listAllFiles(UnixPath.of("")).getFirst();
        var contentString = s3OutputDriver.getFile(file);
        assertTrue(modelHasData(contentString));
    }

    @Test
    void shouldSkipEmptyBatches() throws IOException {
        var batchKey = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(batchKey), StringUtils.EMPTY_STRING);
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
        handler.handleRequest(eventStream(null), outputStream, Mockito.mock(Context.class));

        var emittedEvent = ((StubEventBridgeClient) eventBridgeClient).getLatestEvent();
        assertNull(emittedEvent);
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

        handler.handleRequest(eventStream(null), outputStream, Mockito.mock(Context.class));
        var firstContinuationToken = ((StubEventBridgeClient) eventBridgeClient).getLatestEvent()
                                         .getContinuationToken();
        handler.handleRequest(eventStream(firstBatchKey), outputStream, Mockito.mock(Context.class));
        var secondContinuationToken =
            ((StubEventBridgeClient) eventBridgeClient).getLatestEvent().getContinuationToken();
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
        Executable executable = () -> handler.handleRequest(eventStream(null),
                                                            outputStream,
                                                            mock(Context.class));
        assertThrows(MissingIdException.class, executable);
        assertTrue(loggerAppender.getMessages().contains("Missing id-node in content"));
    }

    private static EventConsumptionAttributes randomConsumptionAttribute() {
        return new EventConsumptionAttributes(DEFAULT_LOCATION, SortableIdentifier.next());
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

    private InputStream eventStream(String continuationToken) throws JsonProcessingException {
        var event = new AwsEventBridgeEvent<KeyBatchRequestEvent>();
        event.setDetail(new KeyBatchRequestEvent(null, continuationToken, randomString(), DEFAULT_LOCATION));
        event.setId(randomString());
        var jsonString = objectMapperWithEmpty.writeValueAsString(event);
        return IoUtils.stringToStream(jsonString);
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

    private static class StubEventBridgeClient implements EventBridgeClient {

        private KeyBatchRequestEvent latestEvent;

        public KeyBatchRequestEvent getLatestEvent() {
            return latestEvent;
        }

        public PutEventsResponse putEvents(PutEventsRequest putEventsRequest) {
            this.latestEvent = saveContainedEvent(putEventsRequest);
            return PutEventsResponse.builder().failedEntryCount(0).build();
        }

        @Override
        public String serviceName() {
            return null;
        }

        @Override
        public void close() {

        }

        private KeyBatchRequestEvent saveContainedEvent(PutEventsRequest putEventsRequest) {
            PutEventsRequestEntry eventEntry = putEventsRequest.entries()
                                                   .stream()
                                                   .collect(SingletonCollector.collect());
            return attempt(eventEntry::detail).map(
                jsonString -> objectMapperWithEmpty.readValue(jsonString, KeyBatchRequestEvent.class)).orElseThrow();
        }
    }
}

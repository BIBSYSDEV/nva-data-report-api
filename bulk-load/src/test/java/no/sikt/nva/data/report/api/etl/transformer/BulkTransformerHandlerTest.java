package no.sikt.nva.data.report.api.etl.transformer;

import static java.util.UUID.randomUUID;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.s3.S3Client;

class BulkTransformerHandlerTest {

    public static final String IDENTIFIER = "__IDENTIFIER__";
    private static final String VALID_PUBLICATION = IoUtils.stringFromResources(Path.of("publication.json"));
    public static final String DEFAULT_LOCATION = "resources";
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
        var handler = new BulkTransformerHandler(s3ResourcesClient,
                                                 s3BatchesClient,
                                                 s3OutputClient,
                                                 eventBridgeClient);
        handler.handleRequest(eventStream(null), outputStream, mock(Context.class));
        var file = s3OutputDriver.listAllFiles(UnixPath.of("")).getFirst();
        var contentString = s3OutputDriver.getFile(file);
        assertTrue(modelHasData(contentString));
    }

    @Test
    void shouldSkipEmptyBatches() throws IOException {
        var batchKey = randomString();
        s3BatchesDriver.insertFile(UnixPath.of(batchKey), StringUtils.EMPTY_STRING);
        var handler = new BulkTransformerHandler(s3ResourcesClient,
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
        var handler = new BulkTransformerHandler(s3ResourcesClient,
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
        var handler = new BulkTransformerHandler(s3ResourcesClient,
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

    private static EventConsumptionAttributes randomConsumptionAttribute() {
        return new EventConsumptionAttributes(DEFAULT_LOCATION, SortableIdentifier.next());
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

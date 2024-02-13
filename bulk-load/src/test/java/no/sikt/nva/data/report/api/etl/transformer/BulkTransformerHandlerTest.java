package no.sikt.nva.data.report.api.etl.transformer;

import static java.util.UUID.randomUUID;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
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

    // TODO: read all data from bucket
    // TODO: convert from JSON-body to Quads
    // TODO: write entire batch as big file (zipped)

    @BeforeEach
    void setup() {
        outputStream = new ByteArrayOutputStream();
        s3ResourcesClient = new FakeS3Client();
        s3ResourcesDriver = new S3Driver(s3ResourcesClient, "resources");
        s3BatchesClient = new FakeS3Client();
        s3BatchesDriver = new S3Driver(s3BatchesClient, "batchesBucket");
        s3OutputClient = new FakeS3Client();
        s3OutputDriver = new S3Driver(s3BatchesClient, "loaderBucket");

        eventBridgeClient = new StubEventBridgeClient();
    }

    @Test
    void shouldReadFromInputBucket() throws IOException {
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
        assertEquals(1, s3OutputDriver.listAllFiles(UnixPath.of("")).size());
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
            () -> objectMapperWithEmpty.readTree(VALID_PUBLICATION.replace(IDENTIFIER, randomUUID().toString()))).orElseThrow();
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

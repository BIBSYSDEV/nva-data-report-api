package no.sikt.nva.data.report.api.export;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.sikt.nva.data.report.testing.utils.generator.TestData;
import no.sikt.nva.data.report.testing.utils.generator.TestData.DatePair;
import no.sikt.nva.data.report.testing.utils.generator.publication.PublicationDate;
import no.sikt.nva.data.report.testing.utils.model.EventConsumptionAttributes;
import no.sikt.nva.data.report.testing.utils.model.IndexDocument;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

class CsvBulkTransformerTest {

    private static final String DEFAULT_LOCATION = "resources";
    private S3Driver s3keyBatches3Driver;
    private S3Client s3keyBatchClient;
    private ByteArrayOutputStream outputStream;
    private S3Client s3OutputClient;
    private S3Driver s3OutputDriver;
    private S3Client s3ResourcesClient;
    private S3Driver s3ResourcesDriver;

    public static EventConsumptionAttributes randomConsumptionAttribute() {
        return new EventConsumptionAttributes(DEFAULT_LOCATION, SortableIdentifier.next());
    }

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        s3keyBatchClient = new FakeS3Client();
        s3keyBatches3Driver = new S3Driver(s3keyBatchClient, "keyBathesBucket");
        s3OutputClient = new FakeS3Client();
        s3OutputDriver = new S3Driver(s3OutputClient, "csvOutputBucket");
        s3ResourcesClient = new FakeS3Client();
        s3ResourcesDriver = new S3Driver(s3ResourcesClient, "resourcesBucket");
    }

    @Test
    void shouldWriteCsvFileToS3() throws IOException {
        var testData = new TestData(generateDatePairs(2));
        var indexDocuments = createAndPersistIndexDocuments(testData);
        var batch = indexDocuments.stream()
                        .map(IndexDocument::getDocumentIdentifier)
                        .collect(Collectors.joining(System.lineSeparator()));
        var batchKey = randomString();
        s3keyBatches3Driver.insertFile(UnixPath.of(batchKey), batch);
        var handler = new CsvBulkTransformer(s3keyBatchClient, s3OutputClient, s3ResourcesClient);
        handler.handleRequest(eventStream(), outputStream, mock(Context.class));
        var actualContent = getActualPersistedFile();
        var expectedContent = testData.getPublicationResponseData();
        assertEquals(expectedContent, actualContent);
    }

    List<DatePair> generateDatePairs(int numberOfDatePairs) {
        return IntStream.range(0, numberOfDatePairs)
                   .mapToObj(i -> new DatePair(new PublicationDate("2024", "02", "02"),
                                               Instant.now().minus(100, ChronoUnit.DAYS)))
                   .toList();
    }

    private List<IndexDocument> createAndPersistIndexDocuments(TestData testData) {
        var indexDocuments = testData.getPublicationTestData().stream()
                                 .map(publication -> new IndexDocument(randomConsumptionAttribute(),
                                                                       IndexDocumentGenerator.createExpandedResource(
                                                                           publication)))
                                 .toList();
        indexDocuments.forEach(document -> document.persistInS3(s3ResourcesDriver));
        return indexDocuments;
    }

    private String getActualPersistedFile() {
        var file = s3OutputDriver.listAllFiles(UnixPath.ROOT_PATH).getFirst();
        return s3OutputDriver.getFile(file);
    }

    private InputStream eventStream() throws JsonProcessingException {
        var event = new AwsEventBridgeEvent<KeyBatchEvent>();
        event.setDetail(new KeyBatchEvent());
        event.setId(randomString());
        var jsonString = JsonUtils.dtoObjectMapper.writeValueAsString(event);
        return IoUtils.stringToStream(jsonString);
    }
}
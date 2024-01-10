package no.sikt.nva.data.report.api.etl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SingleObjectDataLoaderTest {

    public static final ObjectMapper objectMapper = JsonUtils.dtoObjectMapper;

    private static final String BUCKET_NAME = "irrelevant";

    private S3Driver s3Driver;
    private Context context;

    @BeforeEach
    void setup() {
        var s3Client = new FakeS3Client();
        s3Driver = new S3Driver(s3Client, BUCKET_NAME);
        context = new FakeContext();
    }

    @Test
    void shouldLogStuff() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        new SingleObjectDataLoader();
        assertTrue(logAppender.getMessages().contains("Initializing DataLoader"));
    }

    @Test
    void shouldReceiveSqsEvent() throws IOException {
        var path = "event.json";
        var eventBody = IoUtils.inputStreamFromResources(path);
        var fileUri = s3Driver.insertFile(UnixPath.of(path), eventBody);
        var event = new PersistedResourceMessage("someBucket", "someKey");
        var handler = new SingleObjectDataLoader();
        assertDoesNotThrow(() -> handler.handleRequest(event, context));
    }

    //private static SQSEvent createEvent(PersistedResourceMessage persistedResourceMessage) {
    //    var sqsEvent = new SQSEvent();
    //    var message = new SQSMessage();
    //    var body = attempt(() -> objectMapper.writeValueAsString(persistedResourceMessage)).orElseThrow();
    //    message.setBody(body);
    //    sqsEvent.setRecords(List.of(message));
    //    return sqsEvent;
    //}
}

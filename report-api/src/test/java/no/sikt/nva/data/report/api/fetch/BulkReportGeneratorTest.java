package no.sikt.nva.data.report.api.fetch;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.stubs.FakeContext;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BulkReportGeneratorTest {

    private Context context;
    private ByteArrayOutputStream outputStream;
    private BulkReportGenerator handler;

    @BeforeEach
    void setUp() {
        context = new FakeContext();
        outputStream = new ByteArrayOutputStream();
        handler = new BulkReportGenerator();
    }

    @Test
    void shouldRequireReportType() {
        var request = new GenerateReportRequest(null);
        assertThrows(IllegalArgumentException.class,
                     () -> handler.handleRequest(eventStream(request), outputStream, context));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenReportTypeIsInvalid() {
        var request = new GenerateReportRequest(randomString());
        assertThrows(IllegalArgumentException.class,
                     () -> handler.handleRequest(eventStream(request), outputStream, context));
    }

    @Test
    void shouldProduceReportAndPersistInS3() {

    }

    @Test
    void shouldNotNewEventWhenThereAreNoMoreItemsToFetch() {

    }

    @Test
    void shouldEmitNewEventWhenThereAreMoreItemsToFetch() {

    }

    private InputStream eventStream(GenerateReportRequest detail) throws JsonProcessingException {
        var event = new AwsEventBridgeEvent<GenerateReportRequest>();
        event.setDetail(detail);
        event.setId(randomString());
        var jsonString = dtoObjectMapper.writeValueAsString(event);
        return IoUtils.stringToStream(jsonString);
    }
}
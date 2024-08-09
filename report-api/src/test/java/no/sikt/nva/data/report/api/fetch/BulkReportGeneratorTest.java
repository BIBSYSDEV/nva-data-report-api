package no.sikt.nva.data.report.api.fetch;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import commons.model.ReportType;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.stubs.FakeContext;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.logutils.LogUtils;
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
        var request = new BulkReportRequest(null);
        assertThrows(IllegalArgumentException.class,
                     () -> handler.handleRequest(eventStream(request), outputStream, context));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenReportTypeIsInvalid() {
        var request = new BulkReportRequest(randomString());
        assertThrows(IllegalArgumentException.class,
                     () -> handler.handleRequest(eventStream(request), outputStream, context));
    }

    @Test
    void shouldLogReportType() throws JsonProcessingException {
        var reportType = randomElement(ReportType.values()).getType();
        var request = new BulkReportRequest(reportType);
        var appender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(eventStream(request), outputStream, context);
        assertTrue(appender.getMessages().contains(reportType));
    }

    private InputStream eventStream(BulkReportRequest detail) throws JsonProcessingException {
        var event = new AwsEventBridgeEvent<BulkReportRequest>();
        event.setDetail(detail);
        event.setId(randomString());
        var jsonString = dtoObjectMapper.writeValueAsString(event);
        return IoUtils.stringToStream(jsonString);
    }
}
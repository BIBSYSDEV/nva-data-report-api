package no.sikt.nva.data.report.api.etl.loader;

import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.UUID;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

class BulkLoadHandlerTest {

    @Test
    void shouldLogSuccessfulLoadingEvent() throws IOException, InterruptedException {
        final var logger = LogUtils.getTestingAppenderForRootLogger();
        var responseString = createSuccessResponseString();
        var httpClient = setUpSuccessfulHttpResponse(responseString);
        var handler = new BulkLoadHandler(httpClient);
        handler.handleRequest(new ByteArrayInputStream("{}".getBytes()),
                              new ByteArrayOutputStream(),
                              new FakeContext());
        assertTrue(logger.getMessages().contains(responseString));
    }

    @Test
    void shouldLogFailingLoadingEvent() throws IOException, InterruptedException {
        final var logger = LogUtils.getTestingAppenderForRootLogger();
        var responseString = createFailingResponseString();
        var httpClient = setUpFailingHttpResponse(responseString);
        var handler = new BulkLoadHandler(httpClient);
        handler.handleRequest(new ByteArrayInputStream("{}".getBytes()),
                              new ByteArrayOutputStream(),
                              new FakeContext());
        assertTrue(logger.getMessages().contains(responseString));
    }

    @Test
    void shouldLogErrorLog() throws IOException, InterruptedException {
        final var logger = LogUtils.getTestingAppenderForRootLogger();
        var uuid = UUID.randomUUID();
        var responseString = createErrorResponseLogString(uuid);
        var httpClient = setUpErrorLogHttpResponse(responseString);
        var handler = new BulkLoadHandler(httpClient);
        var request = createErrorLogRequest(uuid);
        handler.handleRequest(request, new ByteArrayOutputStream(), new FakeContext());
        assertTrue(logger.getMessages().contains(responseString));
    }

    private InputStream createErrorLogRequest(UUID uuid) {
        return IoUtils.stringToStream(asJson(new ErrorLogRequest(uuid, null, null)));
    }

    private String asJson(ErrorLogRequest errorLogRequest) {
        return attempt(() -> JsonUtils.dtoObjectMapper.writeValueAsString(errorLogRequest))
                   .orElseThrow();
    }

    private String createErrorResponseLogString(UUID uuid) {
        return """
            {
                "status" : "200 OK",
                "payload" : {
                    "failedFeeds" : [ ],
                    "feedCount" : [
                        {
                            "LOAD_FAILED" : 1
                        }
                    ],
                    "overallStatus" : { },
                    "errors" : {
                        "endIndex" : 3,
                        "errorLogs" : [ ],
                        "loadId" : "__UUID__",
                        "startIndex" : 1
                    }
                }
            }
            """.replace("__UUID__", uuid.toString());
    }

    private String createFailingResponseString() {
        return """
            {
                "status" : "403 Forbidden"
            }
            """;
    }

    private HttpClient setUpFailingHttpResponse(String responseString)
        throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        var httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(403);
        when(httpResponse.body()).thenReturn(responseString);
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        return httpClient;
    }

    private static HttpClient setUpSuccessfulHttpResponse(String responseString)
        throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        var httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseString);
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        return httpClient;
    }

    private HttpClient setUpErrorLogHttpResponse(String responseString)
        throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        var httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseString);
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        return httpClient;
    }

    private static String createSuccessResponseString() {
        return """
            {
                "status" : "200 OK",
                "payload" : {
                    "loadId" : "someUUID"
                }
            }
            """;
    }
}

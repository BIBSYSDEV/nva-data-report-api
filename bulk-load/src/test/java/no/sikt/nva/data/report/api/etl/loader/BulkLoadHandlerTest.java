package no.sikt.nva.data.report.api.etl.loader;

import static nva.commons.core.attempt.Try.attempt;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

class BulkLoadHandlerTest {

    private static final String NEPTUNE_ENDPOINT = "NEPTUNE_ENDPOINT";
    private static final String NEPTUNE_PORT = "NEPTUNE_PORT";

    @Test
    void shouldLogSuccessfulLoadingEvent() throws IOException, InterruptedException {
        final var logger = LogUtils.getTestingAppenderForRootLogger();
        var response = new Response(SC_OK, new SuccessPayload(UUID.randomUUID()));
        var httpClient = setUpSuccessfulHttpResponse(response);
        var handler = new BulkLoadHandler(httpClient);
        handler.handleRequest(new ByteArrayInputStream("{}".getBytes()),
                              new ByteArrayOutputStream(),
                              new FakeContext());
        assertTrue(logger.getMessages().contains(response.toString()));
    }

    @Test
    void shouldLogFailingLoadingEvent() throws IOException, InterruptedException {
        final var logger = LogUtils.getTestingAppenderForRootLogger();
        var response = new Response(HttpStatus.SC_FORBIDDEN, new SuccessPayload(UUID.randomUUID()));
        var httpClient = setUpFailingHttpResponse(response);
        var handler = new BulkLoadHandler(httpClient);
        handler.handleRequest(new ByteArrayInputStream("{}".getBytes()),
                              new ByteArrayOutputStream(),
                              new FakeContext());
        assertTrue(logger.getMessages().contains(response.toString()));
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
        assertTrue(logger.getMessages().contains(responseString.toString()));
    }

    private InputStream createErrorLogRequest(UUID uuid) {
        return IoUtils.stringToStream(asJson(new ErrorLogRequest(uuid, null, null)));
    }

    private String asJson(ErrorLogRequest errorLogRequest) {
        return attempt(() -> JsonUtils.dtoObjectMapper.writeValueAsString(errorLogRequest))
                   .orElseThrow();
    }

    private Response createErrorResponseLogString(UUID uuid) {
        return new Response(SC_OK, new ErrorPayload(uuid));
    }

    private HttpClient setUpFailingHttpResponse(Response response)
        throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        var httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(response.status());
        when(httpResponse.body()).thenReturn(response.toString());
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        return httpClient;
    }

    private static HttpClient setUpSuccessfulHttpResponse(Response response)
        throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        var httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(response.status);
        when(httpResponse.body()).thenReturn(response.toString());
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        return httpClient;
    }

    private HttpClient setUpErrorLogHttpResponse(Response response)
        throws IOException, InterruptedException {
        var environment = new Environment();
        var host = environment.readEnv(NEPTUNE_ENDPOINT);
        var port = Integer.parseInt(environment.readEnv(NEPTUNE_PORT));
        var httpClient = mock(HttpClient.class);
        var httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(response.status());
        when(httpResponse.body()).thenReturn(response.toString());
        when(httpClient.send(matchErrorLogRequest(response, host, port), any()))
            .thenReturn(httpResponse);
        return httpClient;
    }

    private static HttpRequest matchErrorLogRequest(Response response, String host, int port) {
        var uri = UriWrapper.fromHost(host, port)
                      .addChild("loader")
                      .addChild(response.payload().loadId().toString())
                      .addQueryParameter("details", "true")
                      .addQueryParameter("errors", "true")
                      .addQueryParameter("page", String.valueOf(1))
                      .addQueryParameter("errorsPerPage", String.valueOf(3))
                      .getUri();

        var request = HttpRequest.newBuilder()
                          .GET()
                          .header("Content-Type", "application/json")
                          .uri(uri)
                          .build();
        return eq(request);
    }

    private record Response(int status, Payload payload) {

        @Override
        public String toString() {
            return String.format("""
                                     {
                                         "status" : "%d",
                                         "payload" : {
                                             "loadId" : "%s"
                                         }
                                     }
                                     """, status, payload.loadId());
        }
    }

    private interface Payload {
        UUID loadId();
    }

    private record SuccessPayload(UUID loadId) implements Payload {

    }

    private record ErrorPayload(UUID loadId) implements Payload {
        @Override
        public String toString() {
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
            """.replace("__UUID__", loadId.toString());
        }
    }
}

package no.sikt.nva.data.report.dbtools;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import no.sikt.nva.data.report.dbtools.exception.DatabaseResetRequestException;
import no.unit.nva.stubs.FakeContext;
import nva.commons.core.Environment;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

class DatabaseResetHandlerTest {

    private static final Context CONTEXT = new FakeContext();
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String NEPTUNE_SYSTEM_TEMPLATE = "https://%s:%s/system";
    private static final Environment environment = new Environment();
    private static final String NEPTUNE_PORT = environment.readEnv("NEPTUNE_PORT");
    private static final String NEPTUNE_ENDPOINT = environment.readEnv("NEPTUNE_ENDPOINT");
    private static final URI DATABASE_ENDPOINT = URI.create(String.format(NEPTUNE_SYSTEM_TEMPLATE,
                                                                          NEPTUNE_ENDPOINT,
                                                                          NEPTUNE_PORT));
    private static final String TEST_TOKEN = UUID.randomUUID().toString();

    @Test
    void shouldSendResetRequestToNeptune() throws IOException, InterruptedException {
        final var logger = LogUtils.getTestingAppenderForRootLogger();
        var initializeRequest = buildInitializationRequest();
        var performRequest = buildPerformRequest();
        var httpClient = mockSuccessfulRequest(initializeRequest, performRequest);
        var handler = new DatabaseResetHandler(httpClient);
        var request = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        handler.handleRequest(request, null, CONTEXT);
        verify(httpClient, times(1)).send(eq(initializeRequest), any());
        verify(httpClient, times(1)).send(eq(performRequest), any());
        assertTrue(logger.getMessages().contains("Successfully submitted reset request"));
    }

    @Test
    void shouldLogFailure() throws IOException, InterruptedException {
        final var logger = LogUtils.getTestingAppenderForRootLogger();
        var httpClient = mockRequestFailure();
        var handler = new DatabaseResetHandler(httpClient);
        var request = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        assertThrows(DatabaseResetRequestException.class, () -> handler.handleRequest(request, null, CONTEXT));
        assertTrue(logger.getMessages().contains("Request failed"));
    }

    @Test
    void shouldCatchInterruptedException() throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        when(httpClient.send(any(), any())).thenThrow(new InterruptedException());
        var handler = new DatabaseResetHandler(httpClient);
        var request = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        var exception = assertThrows(RuntimeException.class, () -> handler.handleRequest(request, null, CONTEXT));
        assertTrue(exception.getMessage().contains("java.lang.InterruptedException"));
    }

    private static HttpClient mockSuccessfulRequest(HttpRequest initializeRequest, HttpRequest performRequest)
        throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        mockClient(initializeRequest, neptuneInitializationResponse(), httpClient);
        //mockClient(performRequest, null, httpClient);
        return httpClient;
    }

    private static void mockClient(HttpRequest request, String responseBody, HttpClient httpClient)
        throws IOException, InterruptedException {
        var httpResponse = createSuccessfulResponse(responseBody);
        when(httpClient.send(eq(request), eq(BodyHandlers.ofString()))).thenReturn(httpResponse);
    }

    @SuppressWarnings("unchecked")
    private static HttpClient mockRequestFailure() throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        var httpResponse = (HttpResponse<String>) mock(HttpResponse.class);
        when(httpClient.send(any(), eq(BodyHandlers.ofString()))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(404);
        when(httpClient.send(any(), eq(BodyHandlers.ofString()))).thenReturn(httpResponse);
        return httpClient;
    }

    private static HttpRequest buildHttpRequest(String body) {
        return HttpRequest.newBuilder()
                   .POST(BodyPublishers.ofString(body))
                   .header(CONTENT_TYPE, "application/json")
                   .uri(DATABASE_ENDPOINT)
                   .build();
    }

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> createSuccessfulResponse(String body) {
        var response = (HttpResponse<String>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        if (nonNull(body)){
            when(response.body()).thenReturn(body);
        }
        return response;
    }

    private static HttpRequest buildPerformRequest() {
        return buildHttpRequest(String.format("""
                                                  { "action" : "performDatabaseReset", "token" : "%s"}
                                                  """, TEST_TOKEN));
    }

    private static HttpRequest buildInitializationRequest() {
        return buildHttpRequest("""
                                    { "action" : "initiateDatabaseReset" }
                                    """);
    }

    private static String neptuneInitializationResponse() {
        return String.format("""
                                 {
                                    "token" : "%s"
                                 }
                                 """, DatabaseResetHandlerTest.TEST_TOKEN);
    }
}
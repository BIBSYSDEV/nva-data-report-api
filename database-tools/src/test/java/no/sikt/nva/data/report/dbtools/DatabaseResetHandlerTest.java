package no.sikt.nva.data.report.dbtools;

import static java.util.Objects.isNull;
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
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import no.sikt.nva.data.report.dbtools.exception.DatabaseResetRequestException;
import no.unit.nva.stubs.FakeContext;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

class DatabaseResetHandlerTest {

    private static final Context CONTEXT = new FakeContext();
    private static final String TEST_TOKEN = UUID.randomUUID().toString();

    @Test
    void shouldSendResetRequestToNeptune() throws IOException, InterruptedException {
        final var logger = LogUtils.getTestingAppenderForRootLogger();
        var httpClient = mockSuccessfulRequest();
        var handler = new DatabaseResetHandler(httpClient);
        var request = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        handler.handleRequest(request, null, CONTEXT);
        verify(httpClient, times(2)).send(any(), any());
        assertTrue(logger.getMessages().contains("Successfully submitted perform reset request"));
    }

    @Test
    void shouldReportNullToken() throws IOException, InterruptedException {
        final var appender = LogUtils.getTestingAppenderForRootLogger();
        var httpClient = mock(HttpClient.class);
        var response = createSuccessfulResponse(neptuneInitializationResponse(null));
        when(httpClient.send(any(), eq(BodyHandlers.ofString()))).thenReturn(response);
        var handler = new DatabaseResetHandler(httpClient);
        var request = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        assertThrows(DatabaseResetRequestException.class, () -> handler.handleRequest(request, null, CONTEXT));
        assertTrue(appender.getMessages().contains("Token is null"));
    }

    @Test
    void shouldLogInitializeFailure() throws IOException, InterruptedException {
        final var logger = LogUtils.getTestingAppenderForRootLogger();
        var httpClient = mockInitializeFailure();
        var handler = new DatabaseResetHandler(httpClient);
        var request = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        assertThrows(DatabaseResetRequestException.class, () -> handler.handleRequest(request, null, CONTEXT));
        assertTrue(logger.getMessages().contains("Initialize database reset request failed"));
    }

    @Test
    void shouldLogPerformResetFailure() throws IOException, InterruptedException {
        final var logger = LogUtils.getTestingAppenderForRootLogger();
        var httpClient = mockPerformResetFailure();
        var handler = new DatabaseResetHandler(httpClient);
        var request = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        assertThrows(DatabaseResetRequestException.class, () -> handler.handleRequest(request, null, CONTEXT));
        assertTrue(logger.getMessages().contains("Perform database reset request failed"));
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

    private static HttpClient mockSuccessfulRequest()
        throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        var successfulInitializeResponse = createSuccessfulResponse(neptuneInitializationResponse(TEST_TOKEN));
        var successfulPerformResetResponse = createSuccessfulResponse(null);
        when(httpClient.send(any(), eq(BodyHandlers.ofString())))
            .thenReturn(successfulInitializeResponse)
            .thenReturn(successfulPerformResetResponse);
        return httpClient;
    }

    private static HttpClient mockInitializeFailure() throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        var httpResponse = getNotFoundResponse();
        when(httpClient.send(any(), eq(BodyHandlers.ofString()))).thenReturn(httpResponse);
        return httpClient;
    }

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> getNotFoundResponse() {
        var httpResponse = (HttpResponse<String>) mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(404);
        return httpResponse;
    }

    private static HttpClient mockPerformResetFailure() throws IOException, InterruptedException {
        var httpClient = mock(HttpClient.class);
        var notFoundResponse = getNotFoundResponse();
        var successfulInitializeResponse = createSuccessfulResponse(neptuneInitializationResponse(
            DatabaseResetHandlerTest.TEST_TOKEN));
        when(httpClient.send(any(), eq(BodyHandlers.ofString())))
            .thenReturn(successfulInitializeResponse)
            .thenReturn(notFoundResponse);
        return httpClient;
    }

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> createSuccessfulResponse(String body) {
        var response = (HttpResponse<String>) mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        if (nonNull(body)) {
            when(response.body()).thenReturn(body);
        } else {
            when(response.body()).thenReturn("");
        }
        return response;
    }

    private static String neptuneInitializationResponse(String token) {
        var string = isNull(token) ? null : "\"" + token + "\"";
        return String.format("""
                                 {
                                    "status": "200 OK",
                                    "payload": {
                                        "token": %s
                                    }
                                 }
                                 """, string);
    }
}
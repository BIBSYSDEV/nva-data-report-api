package no.sikt.nva.data.report.dbtools;

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
import no.unit.nva.stubs.FakeContext;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

class DatabaseResetHandlerTest {

    private static final Context CONTEXT = new FakeContext();

    @Test
    void shouldSendResetRequestToNeptune() throws IOException, InterruptedException {
        final var logger = LogUtils.getTestingAppenderForRootLogger();
        var httpClient = mock(HttpClient.class);
        var httpResponse = (HttpResponse<String>) mock(HttpResponse.class);
        when(httpClient.send(any(), eq(BodyHandlers.ofString()))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(neptuneResponse());
        var handler = new DatabaseResetHandler(httpClient);
        var request = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        handler.handleRequest(request, null, CONTEXT);
        verify(httpClient, times(1)).send(any(), any());
        assertTrue(logger.getMessages().contains("Successfully submitted reset request"));
    }

    private String neptuneResponse() {
        return String.format("""
            {
              "status" : "200 OK",
              "payload" : {
                "token" : "%s"
              }
            }
        """, UUID.randomUUID());
    }
}
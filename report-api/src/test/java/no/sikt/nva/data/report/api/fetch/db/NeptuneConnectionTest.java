package no.sikt.nva.data.report.api.fetch.db;

import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;
import no.sikt.nva.data.report.api.fetch.formatter.CsvFormatter;
import nva.commons.core.Environment;
import org.apache.jena.query.QueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NeptuneConnectionTest {

    public static final Environment ENVIRONMENT = new Environment();
    public static final int PORT = Integer.parseInt(ENVIRONMENT.readEnv("NEPTUNE_PORT"));
    public static final String ENDPOINT = ENVIRONMENT.readEnv("NEPTUNE_ENDPOINT");


    private static HttpClient httpClient;
    private static HttpResponse httpResponse;
    private HttpHeaders httpHeaders;

    @BeforeEach
    void setup() {
        httpClient = mock(HttpClient.class);
        httpResponse = mock(HttpResponse.class);
        httpHeaders = mock(HttpHeaders.class);
    }

    @Test
    void shouldRequestRemoteService() throws IOException, InterruptedException {
        setupMockedServerResponse();

        var query = QueryFactory.create("SELECT * WHERE { ?a a [] } LIMIT 1");
        var result = new NeptuneConnection(httpClient).getResult(query, new CsvFormatter());
        assertNotNull(result);
    }

    private void setupMockedServerResponse() throws IOException, InterruptedException {
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.headers()).thenReturn(httpHeaders);
        when(httpHeaders.firstValue(any())).thenReturn(Optional.of("gzip"))
            .thenReturn(Optional.of(TEXT_CSV.toString()));
        when(httpResponse.body()).thenReturn(compressedResponse());
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
    }

    private InputStream compressedResponse() {
        var input = "a\r\n".getBytes(StandardCharsets.UTF_8);
        var outputStream = new ByteArrayOutputStream();
        try (var gzipOutputStream = new GZIPOutputStream(outputStream)) {
            gzipOutputStream.write(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
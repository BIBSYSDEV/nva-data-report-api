package no.sikt.nva.data.report.dbtools;

import static no.sikt.nva.data.report.dbtools.ResetAction.ACTION_INITIATE_DATABASE_RESET;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import no.sikt.nva.data.report.dbtools.exception.DatabaseResetRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseResetHandler implements RequestStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseResetHandler.class);
    private static final String CONTENT_TYPE = "Content-Type";
    public static final String NEPTUNE_SYSTEM_TEMPLATE = "https://%s:%s/system";
    public static final String NEPTUNE_ENDPOINT = "NEPTUNE_ENDPOINT";
    public static final String NEPTUNE_PORT = "NEPTUNE_PORT";
    public static final int HTTP_OK = 200;
    private final HttpClient httpClient;
    private final Environment environment;

    @JacocoGenerated
    public DatabaseResetHandler() {
        this(HttpClient.newBuilder().build());
    }

    public DatabaseResetHandler(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.environment = new Environment();
    }

    @Override
    public void handleRequest(InputStream inputStream,
                              OutputStream outputStream,
                              Context context) throws IOException {
        try {
            var response = httpClient.send(buildHttpRequest(), BodyHandlers.ofString());
            if (response.statusCode() == HTTP_OK) {
                logger.info("Successfully submitted reset request");
            } else {
                logger.error("Request failed, received status from upstream {}", response.statusCode());
                throw new DatabaseResetRequestException();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private HttpRequest buildHttpRequest() {
        return HttpRequest.newBuilder()
                   .POST(BodyPublishers.ofString(ACTION_INITIATE_DATABASE_RESET))
                   .header(CONTENT_TYPE, "application/json")
                   .uri(getUri())
                   .build();
    }

    private URI getUri() {
        return URI.create(String.format(NEPTUNE_SYSTEM_TEMPLATE,
                                        environment.readEnv(NEPTUNE_ENDPOINT),
                                        environment.readEnv(NEPTUNE_PORT)));
    }
}

package no.sikt.nva.data.report.api.etl.loader;

import static java.util.Objects.isNull;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BulkLoadHandler implements RequestStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(BulkLoadHandler.class);
    private static final String NEPTUNE_ENDPOINT = "NEPTUNE_ENDPOINT";
    private static final String NEPTUNE_PORT = "NEPTUNE_PORT";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String LOADER_IAM_ROLE = "LOADER_IAM_ROLE";
    private static final String AWS_REGION = "AWS_REGION";
    private static final String LOADER_BUCKET = "LOADER_BUCKET";
    private static final int HTTP_OK = 200;
    private static final String LOADER_PATH = "loader";
    private final HttpClient httpClient;

    @JacocoGenerated
    public BulkLoadHandler() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public BulkLoadHandler(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        var errorLogRequest = attempt(() -> JsonUtils.dtoObjectMapper
                                                .readValue(inputStream, ErrorLogRequest.class))
                                  .orElseThrow();
        if (isNull(errorLogRequest.loadId())) {
            executeLoadOperation();
        } else {
            executeLogRequest(errorLogRequest);
        }
    }

    private void executeLoadOperation() {
        var response = attempt(() -> httpClient.send(createRequest(), BodyHandlers.ofString()))
                           .orElseThrow();
        var responseBody = response.body();
        if (response.statusCode() == HTTP_OK) {
            logger.info("Successfully initiated load: {}", responseBody);
        } else {
            logger.error("Loading failed: {}", responseBody);
        }
    }

    private void executeLogRequest(ErrorLogRequest errorLogRequest) {
        var response = attempt(() -> httpClient.send(createLogRequest(errorLogRequest),
                                                     BodyHandlers.ofString())).orElseThrow();
        var responseBody = response.body();
        if (response.statusCode() == HTTP_OK) {
            logger.info("Logs for loadId {}: {}", errorLogRequest.loadId(), responseBody);
        } else {
            logger.error("Log request failed for loadId {}: {}",
                         errorLogRequest.loadId(),
                         responseBody);
        }
    }

    private static HttpRequest createRequest() {
        var environment = new Environment();
        var loaderSpec = createLoaderSpec(environment);
        logger.info("Creating request with spec: {}", loaderSpec);
        return HttpRequest.newBuilder()
                   .POST(BodyPublishers.ofString(loaderSpec))
                   .header(CONTENT_TYPE, APPLICATION_JSON)
                   .uri(createUri(environment))
                   .build();
    }

    private HttpRequest createLogRequest(ErrorLogRequest errorLogRequest) {
        var environment = new Environment();
        var endpoint = environment.readEnv(NEPTUNE_ENDPOINT);
        var port = Integer.parseInt(environment.readEnv(NEPTUNE_PORT));
        return HttpRequest.newBuilder()
                   .GET()
                   .header(CONTENT_TYPE, APPLICATION_JSON)
                   .uri(errorLogRequest.uri(endpoint, port))
                   .build();
    }

    private static URI createUri(Environment environment) {
        var uri = UriWrapper.fromHost(environment.readEnv(NEPTUNE_ENDPOINT),
                                   Integer.parseInt(environment.readEnv(NEPTUNE_PORT)))
                   .addChild(LOADER_PATH)
                   .getUri();
        logger.info("Using URI: {}", uri);
        return uri;
    }

    private static String createLoaderSpec(Environment environment) {
        return new LoaderSpec.Builder()
                   .withSource(getLoaderSpecSource(environment))
                   .withFormat(Format.NQUADS)
                   .withFailOnError(false)
                   .withParallelism(Parallelism.MEDIUM)
                   .withIamRoleArn(environment.readEnv(LOADER_IAM_ROLE))
                   .withQueueRequest(true)
                   .withRegion(environment.readEnv(AWS_REGION))
                   .withUpdateSingleCardinalityProperties(false)
                   .build()
                   .toString();
    }

    private static URI getLoaderSpecSource(Environment environment) {
        return URI.create(environment.readEnv(LOADER_BUCKET));
    }
}

package no.sikt.nva.data.report.api.etl.loader;

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
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BulkLoadHandler implements RequestStreamHandler {

    public static final Logger logger = LoggerFactory.getLogger(BulkLoadHandler.class);
    private static final String URI_TEMPLATE = "https://%s:%s/loader";
    public static final String NEPTUNE_ENDPOINT = "NEPTUNE_ENDPOINT";
    public static final String NEPTUNE_PORT = "NEPTUNE_PORT";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String LOADER_IAM_ROLE = "LOADER_IAM_ROLE";
    public static final String AWS_REGION = "AWS_REGION";
    public static final String LOADER_BUCKET = "LOADER_BUCKET";
    public static final int HTTP_OK = 200;
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
        var response = attempt(() -> httpClient.send(createRequest(), BodyHandlers.ofString())).orElseThrow();
        var responseBody = response.body();
        if (response.statusCode() == HTTP_OK) {
            logger.info("Successfully initiated load: {}", responseBody);
        } else {
            logger.error("Loading failed: {}", responseBody);
        }
    }

    private static HttpRequest createRequest() {
        var environment = new Environment();
        return HttpRequest.newBuilder()
                   .POST(BodyPublishers.ofString(createLoaderSpec(environment)))
                   .header(CONTENT_TYPE, APPLICATION_JSON)
                   .uri(createUri(environment))
                   .build();
    }

    private static URI createUri(Environment environment) {
        return URI.create(String.format(URI_TEMPLATE,
                                        environment.readEnv(NEPTUNE_ENDPOINT),
                                        environment.readEnv(NEPTUNE_PORT)));
    }

    private static String createLoaderSpec(Environment environment) {
        return new LoaderSpec.Builder()
                   .withSource(getLoaderSpecSource(environment))
                   .withFormat(Format.NQUADS)
                   .withFailOnError(true)
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

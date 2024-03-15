package no.sikt.nva.data.report.api.fetch.client;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;
import static nva.commons.core.attempt.Try.attempt;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import no.unit.nva.auth.AuthorizedBackendClient;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NviInstitutionReportClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NviInstitutionReportClient.class);
    private static final String ACCEPT_HEADER = "Accept";
    private static final String CUSTOM_DOMAIN_PATH = "report";
    private static final String INSTITUTION_PATH = "institution";
    private static final String NVI_APPROVAL_PATH = "nvi-approval";
    private static final String QUERY_PARAM_REPORTING_YEAR = "reportingYear";
    private static final String QUERY_PARAM_INSTITUTION_ID = "institutionId";
    private final AuthorizedBackendClient authorizedBackendClient;
    private final String apiHost;

    public NviInstitutionReportClient(AuthorizedBackendClient authorizedBackendClient, String apiHost) {
        this.authorizedBackendClient = authorizedBackendClient;
        this.apiHost = apiHost;
    }

    public String fetchReport(String reportingYear, String topLevelOrganization, String acceptHeader)
        throws ApiGatewayException {
        var request = generateRequest(reportingYear, topLevelOrganization, acceptHeader);
        return attempt(() -> executeRequest(request)).orElseThrow(
            failure -> logAndCreateBadGatewayException(request.build().uri(), failure.getException()));
    }

    private ApiGatewayException logAndCreateBadGatewayException(URI uri, Exception e) {
        LOGGER.error("Unable to reach upstream: {}", uri, e);
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        } else if (e instanceof ApiGatewayException gatewayException) {
            return gatewayException;
        }
        return new BadGatewayException("Unable to reach upstream!");
    }

    private String executeRequest(Builder request)
        throws IOException, InterruptedException, ApiGatewayException {
        var response = authorizedBackendClient.send(request, BodyHandlers.ofString(UTF_8));
        if (HTTP_OK != response.statusCode()) {
            handleError(response);
        }
        return response.body();
    }

    private void handleError(HttpResponse<String> response) throws ApiGatewayException {
        if (HTTP_NOT_FOUND == response.statusCode()) {
            throw new NotFoundException("Report not found!");
        }
        if (HTTP_BAD_REQUEST == response.statusCode()) {
            throw new BadRequestException(response.body());
        }
        LOGGER.error("Error fetching report: {} {}", response.statusCode(), response.body());
        throw new BadGatewayException("Unexpected response from upstream!");
    }

    private Builder generateRequest(String reportingYear, String topLevelOrganization, String acceptHeader) {
        return HttpRequest.newBuilder()
                   .uri(generateUri(reportingYear, topLevelOrganization))
                   .header(ACCEPT_HEADER, acceptHeader)
                   .GET();
    }

    private URI generateUri(String reportingYear, String institutionId) {
        return UriWrapper.fromHost(apiHost)
                   .addChild(CUSTOM_DOMAIN_PATH)
                   .addChild(INSTITUTION_PATH)
                   .addChild(NVI_APPROVAL_PATH)
                   .addQueryParameter(QUERY_PARAM_REPORTING_YEAR, reportingYear)
                   .addQueryParameter(QUERY_PARAM_INSTITUTION_ID, URLEncoder.encode(institutionId, UTF_8))
                   .getUri();
    }
}

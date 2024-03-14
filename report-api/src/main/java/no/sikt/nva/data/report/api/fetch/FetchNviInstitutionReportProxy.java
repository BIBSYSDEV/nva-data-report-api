package no.sikt.nva.data.report.api.fetch;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static com.google.common.net.MediaType.OOXML_SHEET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Optional;
import no.unit.nva.auth.AuthorizedBackendClient;
import no.unit.nva.auth.CognitoCredentials;
import no.unit.nva.auth.uriretriever.BackendClientCredentials;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import nva.commons.secrets.SecretsReader;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchNviInstitutionReportProxy extends ApiGatewayHandler<Void, String> {

    private static final Logger logger = LoggerFactory.getLogger(FetchNviInstitutionReport.class);
    private static final String BACKEND_CLIENT_SECRET_NAME = "BACKEND_CLIENT_SECRET_NAME";
    private static final String BACKEND_CLIENT_AUTH_URL = "BACKEND_CLIENT_AUTH_URL";
    private static final String PATH_PARAMETER_REPORTING_YEAR = "reportingYear";
    private final AuthorizedBackendClient authorizedBackendClient;

    @JacocoGenerated
    public FetchNviInstitutionReportProxy() {
        this(AuthorizedBackendClient.prepareWithCognitoCredentials(readCognitoCredentials()));
    }

    public FetchNviInstitutionReportProxy(AuthorizedBackendClient authorizedBackendClient) {
        super(Void.class);
        this.authorizedBackendClient = authorizedBackendClient;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return List.of(TEXT_CSV, TEXT_PLAIN, MICROSOFT_EXCEL, OOXML_SHEET);
    }

    @Override
    protected String processInput(Void unused, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateAccessRights(requestInfo);
        var reportingYear = requestInfo.getPathParameter(PATH_PARAMETER_REPORTING_YEAR);
        var topLevelOrganization = extractTopLevelOrganization(requestInfo);
        var acceptHeader = requestInfo.getHeader("Accept");
        logRequest(topLevelOrganization, reportingYear);
        return attempt(() -> fetchReport(reportingYear, topLevelOrganization, acceptHeader)).orElseThrow();
    }

    @Override
    protected Integer getSuccessStatusCode(Void unused, String o) {
        return HttpStatus.SC_OK;
    }

    private static CognitoCredentials readCognitoCredentials() {
        return Optional.of(
                new SecretsReader().fetchClassSecret(BACKEND_CLIENT_SECRET_NAME, BackendClientCredentials.class))
                   .map(FetchNviInstitutionReportProxy::mapToCognitoCredentials)
                   .orElseThrow();
    }

    private static CognitoCredentials mapToCognitoCredentials(BackendClientCredentials secret) {
        return new CognitoCredentials(secret::getId,
                                      secret::getSecret,
                                      readCognitoOAuthServerUri());
    }

    private static URI readCognitoOAuthServerUri() {
        return URI.create(new Environment().readEnv(BACKEND_CLIENT_AUTH_URL));
    }

    private static String extractTopLevelOrganization(RequestInfo requestInfo) {
        return requestInfo.getTopLevelOrgCristinId()
                   .map(URI::toString)
                   .orElse("UnknownRequestTopLevelOrganization");
    }

    private static void logRequest(String topLevelOrganization, String reportingYear) {
        logger.info("NVI institution status report requested for organization: {}, reporting year: {}",
                    topLevelOrganization, reportingYear);
    }

    private String fetchReport(String reportingYear, String topLevelOrganization, String acceptHeader)
        throws IOException, InterruptedException {
        var request = generateRequest(reportingYear, topLevelOrganization, acceptHeader);
        return authorizedBackendClient.send(request, BodyHandlers.ofString(UTF_8)).body();
    }

    private Builder generateRequest(String reportingYear, String topLevelOrganization, String acceptHeader) {
        return HttpRequest.newBuilder()
                   .uri(generateUri(reportingYear, topLevelOrganization))
                   .header("Accept", acceptHeader)
                   .GET();
    }

    private URI generateUri(String reportingYear, String topLevelOrganization) {
        return UriWrapper.fromHost(new Environment().readEnv("API_HOST"))
                   .addChild("report")
                   .addChild("nvi-approval")
                   .addQueryParameter("reportingYear", reportingYear)
                   .addQueryParameter("institutionId", topLevelOrganization)
                   .getUri();
    }

    private void validateAccessRights(RequestInfo requestInfo) throws UnauthorizedException {
        if (!requestInfo.userIsAuthorized(AccessRight.MANAGE_NVI)) {
            throw new UnauthorizedException();
        }
    }
}

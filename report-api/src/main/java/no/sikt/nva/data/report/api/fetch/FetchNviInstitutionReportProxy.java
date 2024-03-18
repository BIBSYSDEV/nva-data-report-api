package no.sikt.nva.data.report.api.fetch;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static com.google.common.net.MediaType.OOXML_SHEET;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import no.sikt.nva.data.report.api.fetch.client.NviInstitutionReportClient;
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
import nva.commons.secrets.SecretsReader;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchNviInstitutionReportProxy extends ApiGatewayHandler<Void, String> {

    private static final Logger logger = LoggerFactory.getLogger(FetchNviInstitutionReport.class);
    private static final String API_HOST = "API_HOST";
    private static final String BACKEND_CLIENT_SECRET_NAME = "BACKEND_CLIENT_SECRET_NAME";
    private static final String BACKEND_CLIENT_AUTH_URL = "COGNITO_HOST";
    private static final String ACCEPT_HEADER = "Accept";
    private static final String PATH_PARAMETER_REPORTING_YEAR = "reportingYear";
    private final NviInstitutionReportClient reportClient;

    @JacocoGenerated
    public FetchNviInstitutionReportProxy() {
        this(new NviInstitutionReportClient(
            AuthorizedBackendClient.prepareWithCognitoCredentials(readCognitoCredentials(new Environment())),
            new Environment().readEnv(API_HOST)));
    }

    public FetchNviInstitutionReportProxy(NviInstitutionReportClient reportClient) {
        super(Void.class);
        this.reportClient = reportClient;
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
        var acceptHeader = requestInfo.getHeader(ACCEPT_HEADER);
        setIsBase64EncodedIfContentTypeIsExcel(acceptHeader);
        logRequest(topLevelOrganization, reportingYear);
        return reportClient.fetchReport(reportingYear, topLevelOrganization, acceptHeader);
    }

    @Override
    protected Integer getSuccessStatusCode(Void unused, String o) {
        return HttpStatus.SC_OK;
    }

    @JacocoGenerated
    private static CognitoCredentials readCognitoCredentials(Environment environment) {
        return Optional.of(
                new SecretsReader().fetchClassSecret(environment.readEnv(BACKEND_CLIENT_SECRET_NAME),
                                                     BackendClientCredentials.class))
                   .map(FetchNviInstitutionReportProxy::mapToCognitoCredentials)
                   .orElseThrow();
    }

    @JacocoGenerated
    private static CognitoCredentials mapToCognitoCredentials(BackendClientCredentials secret) {
        return new CognitoCredentials(secret::getId,
                                      secret::getSecret,
                                      readCognitoOAuthServerUri());
    }

    @JacocoGenerated
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

    private static boolean isExcelOrOpenXml(String acceptHeader) {
        return MICROSOFT_EXCEL.toString().equals(acceptHeader) || OOXML_SHEET.toString().equals(acceptHeader);
    }

    private void setIsBase64EncodedIfContentTypeIsExcel(String acceptHeader) {
        if (isExcelOrOpenXml(acceptHeader)) {
            setIsBase64Encoded(true);
        }
    }

    private void validateAccessRights(RequestInfo requestInfo) throws UnauthorizedException {
        if (!requestInfo.userIsAuthorized(AccessRight.MANAGE_NVI)) {
            throw new UnauthorizedException();
        }
    }
}

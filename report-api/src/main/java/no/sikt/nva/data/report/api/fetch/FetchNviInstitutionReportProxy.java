package no.sikt.nva.data.report.api.fetch;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static com.google.common.net.MediaType.OOXML_SHEET;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.URI;
import java.util.List;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchNviInstitutionReportProxy extends ApiGatewayHandler<Void, String> {

    private static final Logger logger = LoggerFactory.getLogger(FetchNviInstitutionReport.class);
    private static final String PATH_PARAMETER_REPORTING_YEAR = "reportingYear";

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return List.of(TEXT_CSV, TEXT_PLAIN, MICROSOFT_EXCEL, OOXML_SHEET);
    }

    public FetchNviInstitutionReportProxy() {
        super(Void.class);
    }

    @Override
    protected String processInput(Void unused, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateAccessRights(requestInfo);
        var reportingYear = requestInfo.getPathParameter(PATH_PARAMETER_REPORTING_YEAR);
        var topLevelOrganization = extractTopLevelOrganization(requestInfo);
        logRequest(topLevelOrganization, reportingYear);
        return null;
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

    private void validateAccessRights(RequestInfo requestInfo) throws UnauthorizedException {
        if (!requestInfo.userIsAuthorized(AccessRight.MANAGE_NVI)) {
            throw new UnauthorizedException();
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void unused, String o) {
        return HttpStatus.SC_OK;
    }
}

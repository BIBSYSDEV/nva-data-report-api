package no.sikt.nva.data.report.api.fetch;

import java.net.URI;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.apigateway.RequestInfo;

public record NviInstitutionReportRequest(String reportingYear,
                                          URI nviOrganization,
                                          String mediaType,
                                          String presignedFileName) implements JsonSerializable {

    private static final String ACCEPT_HEADER = "Accept";
    private static final String PATH_PARAMETER_REPORTING_YEAR = "reportingYear";

    public static NviInstitutionReportRequest from(RequestInfo requestInfo, String fileName) {
        var reportingYear = requestInfo.getPathParameter(PATH_PARAMETER_REPORTING_YEAR);
        var topLevelOrganization = extractTopLevelOrganization(requestInfo);
        var acceptHeader = requestInfo.getHeader(ACCEPT_HEADER);
        return new NviInstitutionReportRequest(reportingYear, topLevelOrganization, acceptHeader, fileName);
    }

    private static URI extractTopLevelOrganization(RequestInfo requestInfo) {
        return requestInfo.getTopLevelOrgCristinId().orElseThrow();
    }
}

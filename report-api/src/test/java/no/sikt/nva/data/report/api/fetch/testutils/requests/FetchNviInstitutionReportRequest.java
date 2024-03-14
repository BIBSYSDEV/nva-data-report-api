package no.sikt.nva.data.report.api.fetch.testutils.requests;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static nva.commons.apigateway.RestRequestHandler.EMPTY_STRING;
import java.net.URLEncoder;
import java.util.Map;

public record FetchNviInstitutionReportRequest(String reportingYear,
                                               String institutionId,
                                               String accept) {

    public Map<String, String> queryParameters() {
        return Map.of("institutionId", URLEncoder.encode(getInstitutionId(), UTF_8),
                      "reportingYear", getReportingYear());
    }

    public Map<String, String> acceptHeader() {
        return Map.of("Accept", accept);
    }

    private String getReportingYear() {
        return isNull(reportingYear) ? EMPTY_STRING : reportingYear;
    }

    private String getInstitutionId() {
        return isNull(institutionId) ? EMPTY_STRING : institutionId;
    }
}
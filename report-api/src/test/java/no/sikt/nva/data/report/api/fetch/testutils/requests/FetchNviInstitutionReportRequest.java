package no.sikt.nva.data.report.api.fetch.testutils.requests;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import java.net.URLEncoder;
import java.util.Map;

public record FetchNviInstitutionReportRequest(String reportingYear,
                                               String institutionId,
                                               String accept) {

    public Map<String, String> queryParameters() {
        return Map.of("institutionId", URLEncoder.encode(getInstitutionId(), UTF_8),
                      "reportingYear", getReportingYear());
    }

    private String getReportingYear() {
        return isNull(reportingYear) ? "" : reportingYear;
    }

    public Map<String, String> acceptHeader() {
        return Map.of("Accept", accept);
    }

    private String getInstitutionId() {
        return isNull(institutionId) ? "" : institutionId;
    }
}
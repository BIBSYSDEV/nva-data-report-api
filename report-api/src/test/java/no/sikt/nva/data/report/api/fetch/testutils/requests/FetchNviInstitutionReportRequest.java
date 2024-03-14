package no.sikt.nva.data.report.api.fetch.testutils.requests;

import java.net.URI;
import java.util.Map;

public record FetchNviInstitutionReportRequest(String reportingYear,
                                               URI institutionId,
                                               String accept) {

    public Map<String, String> pathParameters() {
        return Map.of("reportingYear", reportingYear);
    }

    public Map<String, String> queryParameters() {
        return Map.of("institutionId", institutionId.toString());
    }

    public Map<String, String> acceptHeader() {
        return Map.of("Accept", accept);
    }
}
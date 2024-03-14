package no.sikt.nva.data.report.api.fetch.testutils.requests;

import java.util.Map;

public record FetchNviInstitutionReportProxyRequest(String reportingYear,
                                                    String accept) {

    public Map<String, String> pathParameters() {
        return Map.of("reportingYear", reportingYear);
    }

    public Map<String, String> acceptHeader() {
        return Map.of("Accept", accept);
    }
}
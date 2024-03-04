package no.sikt.nva.data.report.api.fetch.testutils.requests;

import java.util.Map;

public record FetchNviInstitutionReportRequest(String year,
                                               String accept) {

    public Map<String, String> pathParameters() {
        return Map.of("year", year);
    }

    public Map<String, String> acceptHeader() {
        return Map.of("Accept", accept);
    }
}
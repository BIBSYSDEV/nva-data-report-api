package no.sikt.nva.data.report.api.fetch.testutils.requests;

import java.util.Map;

public record FetchNviInstitutionReportRequest(String accept) {

    public Map<String, String> acceptHeader() {
        return Map.of("Accept", accept);
    }
}
package no.sikt.nva.data.report.api.fetch.testutils;

import java.util.Map;

public record TestingRequest(String accept,
                             String reportType,
                             String before,
                             String after,
                             String offset,
                             String pageSize) {

    public Map<String, String> queryParameters() {
        return Map.of(
            "before", before,
            "after", after,
            "offset", offset,
            "pageSize", pageSize
        );
    }

    public Map<String, String> pathParameters() {
        return Map.of("type", reportType);
    }

    public Map<String, String> acceptHeader() {
        return Map.of("Accept", accept);
    }
}
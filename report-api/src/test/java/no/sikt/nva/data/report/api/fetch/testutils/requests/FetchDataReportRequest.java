package no.sikt.nva.data.report.api.fetch.testutils.requests;

import java.util.Map;

import static java.util.Objects.isNull;

public record FetchDataReportRequest(String accept,
                                     String reportType,
                                     String before,
                                     String after,
                                     String cursor,
                                     String pageSize) {

    public FetchDataReportRequest {
        if (isNull(cursor) || cursor.isBlank()) {
            cursor = "";
        }
    }

    public Map<String, String> queryParameters() {
        return Map.of(
            "before", before,
            "after", after,
            "cursor", cursor,
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
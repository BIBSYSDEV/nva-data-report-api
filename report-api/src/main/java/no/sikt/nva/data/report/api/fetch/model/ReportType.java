package no.sikt.nva.data.report.api.fetch.model;

import java.util.Arrays;
import java.util.stream.Collectors;
import nva.commons.apigateway.exceptions.BadRequestException;

public enum ReportType {
    AFFILIATION("affiliation"),
    CONTRIBUTOR("contributor"),
    FUNDING("funding"),
    IDENTIFIER("identifier"),
    PUBLICATION("publication");

    private final String type;

    ReportType(String type) {
        this.type = type;
    }

    public static ReportType parse(String candidate) throws BadRequestException {
        return Arrays.stream(values())
                   .filter(reportType -> reportType.getType().equals(candidate))
                   .findAny()
                   .orElseThrow(ReportType::getBadRequest);
    }

    private static BadRequestException getBadRequest() {
        var message = String.format("Bad request. Acceptable report types: %s",
                                    getValidReportTypes());
        return new BadRequestException(message);
    }

    private static String getValidReportTypes() {
        return Arrays.stream(values())
                   .map(ReportType::getType)
                   .collect(Collectors.joining(", "));
    }

    public String getType() {
        return type;
    }
}

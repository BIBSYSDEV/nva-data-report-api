package no.sikt.nva.data.report.api.fetch.model;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum ReportType {
    AFFILIATION("affiliation"),
    CONTRIBUTOR("contributor"),
    FUNDING("funding"),
    IDENTIFIER("identifier"),
    PUBLICATION("publication"),
    NVI("nvi");

    public static final String DELIMITER = ", ";
    public static final String BAD_REQUEST_MESSAGE_TEMPLATE = "Illegal argument. Acceptable report types: %s";
    private final String type;

    ReportType(String type) {
        this.type = type;
    }

    public static ReportType parse(String candidate) {
        return Arrays.stream(values())
                   .filter(reportType -> reportType.getType().equals(candidate))
                   .findAny()
                   .orElseThrow(ReportType::getIllegalArgument);
    }

    public String getType() {
        return type;
    }

    private static IllegalArgumentException getIllegalArgument() {
        var message = String.format(BAD_REQUEST_MESSAGE_TEMPLATE, getValidReportTypes());
        return new IllegalArgumentException(message);
    }

    private static String getValidReportTypes() {
        return Arrays.stream(values())
                   .map(ReportType::getType)
                   .collect(Collectors.joining(DELIMITER));
    }
}

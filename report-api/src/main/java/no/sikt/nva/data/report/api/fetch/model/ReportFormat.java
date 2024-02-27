package no.sikt.nva.data.report.api.fetch.model;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;

public enum ReportFormat {
    CSV,
    TEXT,
    EXCEL;

    public static ReportFormat fromString(String reportFormat) {
        if (TEXT_PLAIN.toString().equalsIgnoreCase(reportFormat)) {
            return TEXT;
        } else if (MICROSOFT_EXCEL.toString().equalsIgnoreCase(reportFormat)) {
            return EXCEL;
        } else {
            return CSV;
        }
    }
}

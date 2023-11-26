package no.sikt.nva.data.report.api.fetch.model;

import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;

public enum ReportFormat {
    CSV,
    TEXT;

    public static ReportFormat fromString(String reportFormat) {
        return TEXT_PLAIN.toString().equalsIgnoreCase(reportFormat) ? TEXT : CSV;
    }
}

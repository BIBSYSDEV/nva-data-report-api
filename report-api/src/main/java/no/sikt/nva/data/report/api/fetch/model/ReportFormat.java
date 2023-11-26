package no.sikt.nva.data.report.api.fetch.model;

public enum ReportFormat {
    CSV,
    TEXT;

    public static ReportFormat fromString(String reportFormat) {
        return "text/plain".equalsIgnoreCase(reportFormat) ? TEXT : CSV;
    }
}

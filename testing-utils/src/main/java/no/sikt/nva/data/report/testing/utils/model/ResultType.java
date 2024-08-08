package no.sikt.nva.data.report.testing.utils.model;

public enum ResultType {
    CSV,
    TEXT_PLAIN;

    public static ResultType fromString(String type) {
        return switch (type) {
            case "CSV" -> CSV;
            case "TEXT" -> TEXT_PLAIN;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}

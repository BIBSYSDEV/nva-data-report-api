package no.sikt.nva.data.report.api.export;

import java.util.Arrays;
import java.util.stream.Stream;

public enum DocumentType {
    PUBLICATION("resources"),
    NVI_CANDIDATE("nvi-candidates");

    private final String keyPrefix;

    DocumentType(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public static DocumentType fromLocation(String location) {
        return Stream.of(values())
                   .filter(type -> type.getKeyPrefix().equalsIgnoreCase(location))
                   .findFirst()
                   .orElseThrow(DocumentType::getIllegalArgument);
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    private static IllegalArgumentException getIllegalArgument() {
        var message = String.format("Illegal argument. Acceptable locations are: %s", Arrays.toString(values()));
        return new IllegalArgumentException(message);
    }
}

package no.sikt.nva.data.report.api.etl.model;

import java.util.stream.Stream;

public enum EventType {
    UPSERT("PutObject"),
    DELETE("DeleteObject");
    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public static EventType parse(String candidate) {
        return Stream.of(values())
                   .filter(eventType -> eventType.value.equals(candidate))
                   .findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Unknown event type: " + candidate));
    }

    public String getValue() {
        return value;
    }
}

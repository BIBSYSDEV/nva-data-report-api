package no.sikt.nva.data.report.testing.utils.generator.publication;

import java.util.Arrays;

public enum SampleLevel {
    LEVEL_ONE("LevelOne"), LEVEL_TWO("LevelTwo");

    private final String value;

    SampleLevel(String value) {
        this.value = value;
    }

    public static SampleLevel parse(String value) {
        return Arrays.stream(values())
                   .filter(level -> level.getValue().equalsIgnoreCase(value))
                   .findFirst()
                   .orElseThrow();
    }

    public String getValue() {
        return value;
    }
}

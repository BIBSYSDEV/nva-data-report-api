package no.sikt.nva.data.report.testing.utils.generator.publication;

import java.util.Arrays;

public enum TestLevel {
    LEVEL_ONE("LevelOne"), LEVEL_TWO("LevelTwo");

    private final String value;

    TestLevel(String value) {
        this.value = value;
    }

    public static TestLevel parse(String value) {
        return Arrays.stream(TestLevel.values())
                   .filter(level -> level.getValue().equalsIgnoreCase(value))
                   .findFirst()
                   .orElseThrow();
    }

    public String getValue() {
        return value;
    }
}

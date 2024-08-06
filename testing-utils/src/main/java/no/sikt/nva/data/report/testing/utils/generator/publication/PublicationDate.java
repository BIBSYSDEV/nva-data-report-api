package no.sikt.nva.data.report.testing.utils.generator.publication;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record PublicationDate(String year, String month, String day) {

    public static final String DELIMITER = "-";

    public String getIsoDate() {
        return Stream.of(year, month, day)
                   .filter(Objects::nonNull)
                   .collect(Collectors.joining(DELIMITER));
    }
}

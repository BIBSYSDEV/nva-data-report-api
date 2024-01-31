package no.sikt.nva.data.report.api.fetch.testutils;

import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ValidRequestSource implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        return Stream.of(
            Arguments.of(
                Named.of("nvi — text/csv", new TestingRequest(
                             TEXT_CSV.toString(),
                             "nvi",
                             "2024-01-01",
                             "1998-01-01",
                             "0",
                             "10"
                         )
                )
            ),
            Arguments.of(
                Named.of("nvi — text/plain", new TestingRequest(
                             TEXT_PLAIN.toString(),
                             "nvi",
                             "2024-01-01",
                             "1998-01-01",
                             "0",
                             "10"
                         )
                )
            )
        );
    }
}

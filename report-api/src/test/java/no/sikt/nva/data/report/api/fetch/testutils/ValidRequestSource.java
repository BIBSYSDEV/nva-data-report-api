package no.sikt.nva.data.report.api.fetch.testutils;

import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import java.time.Instant;
import java.util.stream.Stream;
import no.sikt.nva.data.report.api.fetch.testutils.requests.FetchDataReportRequest;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ValidRequestSource implements ArgumentsProvider {

    private static final String PAGE_SIZE = "100";
    private static final String OFFSET = "0";
    private static final String NOW = Instant.now().toString();

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        return Stream.of(
            Arguments.of(
                Named.of("nvi — text/csv", new FetchDataReportRequest(
                             TEXT_CSV.toString(),
                             "nvi",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("nvi — text/plain", new FetchDataReportRequest(
                             TEXT_PLAIN.toString(),
                             "nvi",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            )
        );
    }
}
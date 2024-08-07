package no.sikt.nva.data.report.api.fetch.testutils;

import static no.sikt.nva.data.report.api.fetch.model.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.model.CustomMediaType.TEXT_PLAIN;
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
                Named.of("Allows full datetime", new FetchDataReportRequest(
                             TEXT_CSV.toString(),
                             "affiliation",
                             NOW,
                             "1998-01-01T05:09:32Z",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("affiliation — text/csv", new FetchDataReportRequest(
                             TEXT_CSV.toString(),
                             "affiliation",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("affiliation — text/plain", new FetchDataReportRequest(
                             TEXT_PLAIN.toString(),
                             "affiliation",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("contributor — text/csv", new FetchDataReportRequest(
                             TEXT_CSV.toString(),
                             "contributor",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("contributor — text/plain", new FetchDataReportRequest(
                             TEXT_PLAIN.toString(),
                             "contributor",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("funding — text/csv", new FetchDataReportRequest(
                             TEXT_CSV.toString(),
                             "funding",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("funding — text/plain", new FetchDataReportRequest(
                             TEXT_PLAIN.toString(),
                             "funding",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("identifier — text/csv", new FetchDataReportRequest(
                             TEXT_CSV.toString(),
                             "identifier",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("identifier — text/plain", new FetchDataReportRequest(
                             TEXT_PLAIN.toString(),
                             "identifier",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("publication — text/csv", new FetchDataReportRequest(
                             TEXT_CSV.toString(),
                             "publication",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("publication — text/plain", new FetchDataReportRequest(
                             TEXT_PLAIN.toString(),
                             "publication",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
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
package no.sikt.nva.data.report.api.fetch.testutils;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ValidRequestSource implements ArgumentsProvider {

    private static final String PAGE_SIZE = "100";
    public static final String OFFSET = "0";

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        return Stream.of(
            Arguments.of(
                Named.of("Allows full datetime", new TestingRequest(
                             TEXT_CSV.toString(),
                             "affiliation",
                             "2024-01-01T03:02:11Z",
                             "1998-01-01T05:09:32Z",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("affiliation — text/csv", new TestingRequest(
                             TEXT_CSV.toString(),
                             "affiliation",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("affiliation — text/plain", new TestingRequest(
                             TEXT_PLAIN.toString(),
                             "affiliation",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("contributor — text/csv", new TestingRequest(
                             TEXT_CSV.toString(),
                             "contributor",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("contributor — text/plain", new TestingRequest(
                             TEXT_PLAIN.toString(),
                             "contributor",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("funding — text/csv", new TestingRequest(
                             TEXT_CSV.toString(),
                             "funding",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("funding — text/plain", new TestingRequest(
                             TEXT_PLAIN.toString(),
                             "funding",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("identifier — text/csv", new TestingRequest(
                             TEXT_CSV.toString(),
                             "identifier",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("identifier — text/plain", new TestingRequest(
                             TEXT_PLAIN.toString(),
                             "identifier",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("publication — text/csv", new TestingRequest(
                             TEXT_CSV.toString(),
                             "publication",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("publication — text/plain", new TestingRequest(
                             TEXT_PLAIN.toString(),
                             "publication",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("nvi — text/csv", new TestingRequest(
                             TEXT_CSV.toString(),
                             "nvi",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("nvi — text/plain", new TestingRequest(
                             TEXT_PLAIN.toString(),
                             "nvi",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("nvi — application/vnd.ms-excel", new TestingRequest(
                             MICROSOFT_EXCEL.toString(),
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

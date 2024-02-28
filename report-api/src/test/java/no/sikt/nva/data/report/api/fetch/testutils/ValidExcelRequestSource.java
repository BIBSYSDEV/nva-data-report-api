package no.sikt.nva.data.report.api.fetch.testutils;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ValidExcelRequestSource implements ArgumentsProvider {

    private static final String PAGE_SIZE = "100";
    private static final String OFFSET = "0";

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        return Stream.of(
            Arguments.of(
                Named.of("affiliation — application/vnd.ms-excel", new TestingRequest(
                             MICROSOFT_EXCEL.toString(),
                             "affiliation",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("contributor — application/vnd.ms-excel", new TestingRequest(
                             MICROSOFT_EXCEL.toString(),
                             "contributor",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("funding — application/vnd.ms-excel", new TestingRequest(
                             MICROSOFT_EXCEL.toString(),
                             "funding",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("identifier — application/vnd.ms-excel", new TestingRequest(
                             MICROSOFT_EXCEL.toString(),
                             "identifier",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("publication — application/vnd.ms-excel", new TestingRequest(
                             MICROSOFT_EXCEL.toString(),
                             "publication",
                             "2024-01-01",
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(Named.of("nvi — application/vnd.ms-excel", new TestingRequest(
                                      MICROSOFT_EXCEL.toString(),
                                      "nvi",
                                      "2024-01-01",
                                      "1998-01-01",
                                      OFFSET,
                                      PAGE_SIZE
                                  )
                         )
            )
        );
    }
}

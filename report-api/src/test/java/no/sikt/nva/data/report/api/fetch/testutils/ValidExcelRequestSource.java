package no.sikt.nva.data.report.api.fetch.testutils;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static com.google.common.net.MediaType.OOXML_SHEET;
import java.time.Instant;
import java.util.stream.Stream;
import no.sikt.nva.data.report.api.fetch.testutils.requests.FetchDataReportRequest;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ValidExcelRequestSource implements ArgumentsProvider {

    private static final String NOW = Instant.now().toString();
    private static final String PAGE_SIZE = "100";
    private static final String OFFSET = "0";

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        return Stream.of(
            Arguments.of(
                Named.of("affiliation — application/vnd.ms-excel", new FetchDataReportRequest(
                             MICROSOFT_EXCEL.toString(),
                             "affiliation",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("affiliation — application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                         new FetchDataReportRequest(
                             OOXML_SHEET.toString(),
                             "affiliation",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("contributor — application/vnd.ms-excel", new FetchDataReportRequest(
                             MICROSOFT_EXCEL.toString(),
                             "contributor",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("contributor — application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                         new FetchDataReportRequest(
                             OOXML_SHEET.toString(),
                             "contributor",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("funding — application/vnd.ms-excel", new FetchDataReportRequest(
                             MICROSOFT_EXCEL.toString(),
                             "funding",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("funding — application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                         new FetchDataReportRequest(
                             OOXML_SHEET.toString(),
                             "funding",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("identifier — application/vnd.ms-excel", new FetchDataReportRequest(
                             MICROSOFT_EXCEL.toString(),
                             "identifier",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("identifier — application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                         new FetchDataReportRequest(
                             OOXML_SHEET.toString(),
                             "identifier",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("publication — application/vnd.ms-excel", new FetchDataReportRequest(
                             MICROSOFT_EXCEL.toString(),
                             "publication",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(
                Named.of("publication — application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                         new FetchDataReportRequest(
                             OOXML_SHEET.toString(),
                             "publication",
                             NOW,
                             "1998-01-01",
                             OFFSET,
                             PAGE_SIZE
                         )
                )
            ),
            Arguments.of(Named.of("nvi — application/vnd.ms-excel", new FetchDataReportRequest(
                                      MICROSOFT_EXCEL.toString(),
                                      "nvi",
                                      NOW,
                                      "1998-01-01",
                                      OFFSET,
                                      PAGE_SIZE
                                  )
                         )
            ),
            Arguments.of(
                Named.of("nvi — application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                         new FetchDataReportRequest(
                             OOXML_SHEET.toString(),
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

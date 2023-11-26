package no.sikt.nva.data.report.api.fetch.testutils;

import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class BadRequestProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        return Stream.of(
            Arguments.of(Named.of("Bad report type",
                                  new TestingRequest("text/plain",
                                                     "weird",
                                                     "2012-01-01",
                                                     "2023-12-31", "0",
                                                     "10"))),
            Arguments.of(Named.of("Bad before date",
                                  new TestingRequest(
                                      "text/plain",
                                      "identifier",
                                      "2012-0101",
                                      "2023-12-31", "0",
                                      "10"))
            ),
            Arguments.of(Named.of("Bad after date",
                                  new TestingRequest("text/plain",
                                                     "identifier",
                                                     "2012-01-01",
                                                     "2023-1231",
                                                     "0",
                                                     "10"))),
            Arguments.of(Named.of("Before is before after date",
                                  new TestingRequest("text/plain",
                                                     "identifier",
                                                     "2019-01-01",
                                                     "2020-12-31",
                                                     "0",
                                                     "10"))
            ),
            Arguments.of(Named.of("Offset is negative number",
                                  new TestingRequest("text/plain",
                                                     "identifier",
                                                     "2020-01-01",
                                                     "2020-12-31",
                                                     "-10",
                                                     "10"))),
            Arguments.of(Named.of("Offset is negative number",
                                  new TestingRequest("text/plain",
                                                     "identifier",
                                                     "2020-01-01",
                                                     "2020-12-31",
                                                     "10",
                                                     "-10"))
            )
        );
    }
}

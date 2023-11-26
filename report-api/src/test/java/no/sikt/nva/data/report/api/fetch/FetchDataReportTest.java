package no.sikt.nva.data.report.api.fetch;

import static no.sikt.nva.data.report.api.fetch.FetchDataReport.TEXT_CSV;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import no.sikt.nva.data.report.api.fetch.db.DatabaseConnection;
import no.sikt.nva.data.report.api.fetch.db.FakeDatabaseConnection;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import no.sikt.nva.data.report.api.fetch.testutils.BadRequestProvider;
import no.sikt.nva.data.report.api.fetch.testutils.TestingRequest;
import no.sikt.nva.data.report.api.fetch.testutils.ValidRequestSource;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

class FetchDataReportTest {

    public static final String CRLF = "\r\n";
    public static final String LF = "\n";
    public static final String CSV_FILE_EXTENSION = ".csv";
    public static final String TXT_FILE_EXTENSION = ".txt";

    @ParameterizedTest()
    @DisplayName("Should throw BadRequestException when input is invalid")
    @ArgumentsSource(BadRequestProvider.class)
    void shouldThrowBadRequest(TestingRequest report)
        throws IOException {
        var service = new QueryService(setupDatabaseConnection());
        var handler = new FetchDataReport(service);
        var output = new ByteArrayOutputStream();
        handler.handleRequest(generateHandlerRequest(report), output, new FakeContext());
        var response = GatewayResponse.fromOutputStream(output, String.class);
        assertEquals(400, response.getStatusCode());
    }

    @ParameterizedTest
    @ArgumentsSource(ValidRequestSource.class)
    void shouldReturnFormattedResult(TestingRequest request) throws IOException {
        var service = new QueryService(setupDatabaseConnection());
        var handler = new FetchDataReport(service);
        var input = generateHandlerRequest(request);
        var output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, new FakeContext());
        var response = GatewayResponse.fromOutputStream(output, String.class);
        assertEquals(200, response.getStatusCode());
        var expected = getExpected(request);
        assertEquals(expected, response.getBody());
    }

    // TODO: Craft queries and data to test every SELECT clause, BEFORE/AFTER/OFFSET/PAGE_SIZE.
    private String getExpected(TestingRequest request) {
        var acceptHeader = request.acceptHeader().get(ACCEPT);
        var fileExtension = TEXT_CSV.toString().equals(acceptHeader) ? CSV_FILE_EXTENSION : TXT_FILE_EXTENSION;

        var data = IoUtils.stringFromResources(Path.of("expected_response", request.reportType() + fileExtension));
        return CSV_FILE_EXTENSION.equals(fileExtension) ? reformatCsv(data) : data;
    }

    private static String reformatCsv(String data) {
        return data.replace(LF, CRLF) + CRLF;
    }

    private static InputStream generateHandlerRequest(TestingRequest request) throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(JsonUtils.dtoObjectMapper)
                   .withHeaders(request.acceptHeader())
                   .withPathParameters(request.pathParameters())
                   .withQueryParameters(request.queryParameters())
                   .build();
    }

    private DatabaseConnection setupDatabaseConnection() {
        var databaseConnection = new FakeDatabaseConnection();
        var data = IoUtils.stringFromResources(Path.of("example_data", "017c310cab3a-5f71edea-621b-403c-8138"
                                                                       + "-9d598cdb4020.nt"));
        databaseConnection.insert(data);
        return databaseConnection;
    }
}

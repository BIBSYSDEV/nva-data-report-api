package no.sikt.nva.data.report.api.fetch;

import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedCsvFormatter.generateTable;
import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedExcelFormatter.generateExcel;
import static no.sikt.nva.data.report.api.fetch.formatter.ResultSorter.sortResponse;
import static no.sikt.nva.data.report.api.fetch.testutils.ExcelAsserter.assertEqualsInAnyOrder;
import static nva.commons.apigateway.GatewayResponse.fromOutputStream;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.stream.Stream;
import no.sikt.nva.data.report.api.fetch.model.ReportFormat;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import no.sikt.nva.data.report.api.fetch.testutils.generator.TestData;
import no.sikt.nva.data.report.api.fetch.testutils.requests.FetchNviInstitutionReportRequest;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import org.apache.jena.riot.Lang;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class FetchNviInstitutionReportTest extends LocalFusekiTest {

    @ParameterizedTest
    @MethodSource("fetchNviInstitutionReportRequestProvider")
    void shouldReturnFormattedResult(FetchNviInstitutionReportRequest request) throws IOException {
        var testData = new TestData(generateDatePairs(2));
        databaseConnection.write(GRAPH, toTriples(testData.getModel()), Lang.NTRIPLES);
        var handler = new FetchNviInstitutionReport();
        var input = generateHandlerRequest(request);
        var output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, new FakeContext());
        var response = fromOutputStream(output, String.class);
        assertEquals(200, response.getStatusCode());
        var expected = getExpected(request, testData);
        var sortedResponse = sortResponse(getReportFormat(request), response.getBody());
        assertEquals(expected, sortedResponse);
    }

    @ParameterizedTest
    @MethodSource("fetchNviInstitutionReportExcelRequestProvider")
    void shouldReturnBase64EncodedOutputStreamWhenContentTypeIsExcel(FetchNviInstitutionReportRequest request)
        throws IOException {
        var testData = new TestData(generateDatePairs(2));
        databaseConnection.write(GRAPH, toTriples(testData.getModel()), Lang.NTRIPLES);
        var handler = new FetchNviInstitutionReport();
        var input = generateHandlerRequest(request);
        var output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, new FakeContext());
        var response = fromOutputStream(output, String.class);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getIsBase64Encoded());
    }

    @ParameterizedTest
    @MethodSource("fetchNviInstitutionReportExcelRequestProvider")
    void shouldReturnDataInExcelSheetWhenContentTypeIsExcel(FetchNviInstitutionReportRequest request)
        throws IOException {
        var testData = new TestData(generateDatePairs(2));
        databaseConnection.write(GRAPH, toTriples(testData.getModel()), Lang.NTRIPLES);
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var input = generateHandlerRequest(request);
        var output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, new FakeContext());
        var expected = getExpectedExcel(testData);
        var decodedResponse = Base64.getDecoder().decode(fromOutputStream(output, String.class).getBody());
        var actual = new Excel(new XSSFWorkbook(new ByteArrayInputStream(decodedResponse)));
        assertEqualsInAnyOrder(expected, actual);
    }

    private static Stream<Arguments> fetchNviInstitutionReportRequestProvider() {
        return Stream.of(Arguments.of(new FetchNviInstitutionReportRequest("text/plain")),
                         Arguments.of(new FetchNviInstitutionReportRequest("text/csv")));
    }

    private static Stream<Arguments> fetchNviInstitutionReportExcelRequestProvider() {
        return Stream.of(Arguments.of(new FetchNviInstitutionReportRequest("application/vnd.ms-excel")),
                         Arguments.of(new FetchNviInstitutionReportRequest(
                             "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")));
    }

    private static InputStream generateHandlerRequest(FetchNviInstitutionReportRequest request)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(JsonUtils.dtoObjectMapper)
                   .withHeaders(request.acceptHeader())
                   .build();
    }

    private static ReportFormat getReportFormat(FetchNviInstitutionReportRequest request) {
        return ReportFormat.fromMediaType(request.acceptHeader().get(ACCEPT));
    }

    private String getExpected(FetchNviInstitutionReportRequest request, TestData test) {
        var reportFormat = getReportFormat(request);
        var data = test.getNviInstitutionStatusResponseData();
        return ReportFormat.CSV.equals(reportFormat)
                   ? data
                   : generateTable(data);
    }

    private Excel getExpectedExcel(TestData test) {
        return generateExcel(test.getNviInstitutionStatusResponseData());
    }
}

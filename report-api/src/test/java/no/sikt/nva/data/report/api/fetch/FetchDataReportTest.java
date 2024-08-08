package no.sikt.nva.data.report.api.fetch;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static java.lang.String.valueOf;
import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedCsvFormatter.generateTable;
import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedExcelFormatter.generateExcel;
import static no.sikt.nva.data.report.api.fetch.model.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.model.CustomMediaType.TEXT_PLAIN;
import static no.sikt.nva.data.report.api.fetch.testutils.ExcelAsserter.assertEqualsInAnyOrder;
import static no.sikt.nva.data.report.testing.utils.ResultSorter.extractDataLines;
import static no.sikt.nva.data.report.testing.utils.ResultSorter.sortResponse;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_ID;
import static nva.commons.apigateway.GatewayResponse.fromOutputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.fasterxml.jackson.core.JsonProcessingException;
import commons.model.ReportType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import no.sikt.nva.data.report.api.fetch.model.ReportFormat;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import no.sikt.nva.data.report.api.fetch.testutils.BadRequestProvider;
import no.sikt.nva.data.report.api.fetch.testutils.ValidExcelRequestSource;
import no.sikt.nva.data.report.api.fetch.testutils.ValidRequestSource;
import no.sikt.nva.data.report.api.fetch.testutils.requests.FetchDataReportRequest;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;
import no.sikt.nva.data.report.testing.utils.generator.TestData;
import no.sikt.nva.data.report.testing.utils.model.ResultType;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

class FetchDataReportTest extends LocalFusekiTest {

    private static final String OFFSET_ZERO = "0";
    private static final String OFFSET_ONE = "1";

    @ParameterizedTest()
    @DisplayName("Should throw BadRequestException when input is invalid")
    @ArgumentsSource(BadRequestProvider.class)
    void shouldThrowBadRequest(FetchDataReportRequest report)
        throws IOException {
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var output = executeRequest(handler, generateHandlerRequest(report));
        var response = fromOutputStream(output, String.class);
        assertEquals(400, response.getStatusCode());
    }

    @ParameterizedTest
    @ArgumentsSource(ValidRequestSource.class)
    void shouldReturnFormattedResult(FetchDataReportRequest request) throws IOException, BadRequestException {
        var testData = new TestData(generateDatePairs(2));
        loadModels(testData.getModels());
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var input = generateHandlerRequest(request);
        var output = executeRequest(handler, input);
        var response = fromOutputStream(output, String.class);
        assertEquals(200, response.getStatusCode());
        var expected = getExpected(request, testData);
        var sortedResponse = sortResponse(ResultType.fromString(getReportFormat(request).toString()), response.getBody(), PUBLICATION_ID,
                                          CONTRIBUTOR_IDENTIFIER);
        assertEquals(expected, sortedResponse);
    }

    @ParameterizedTest
    @ArgumentsSource(ValidExcelRequestSource.class)
    void shouldReturnBase64EncodedOutputStreamWhenContentTypeIsExcel(FetchDataReportRequest request)
        throws IOException {
        var testData = new TestData(generateDatePairs(2));
        loadModels(testData.getModels());
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var input = generateHandlerRequest(request);
        var output = executeRequest(handler, input);
        var response = fromOutputStream(output, String.class);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getIsBase64Encoded());
    }

    @ParameterizedTest
    @ArgumentsSource(ValidExcelRequestSource.class)
    void shouldReturnDataInExcelSheetWhenContentTypeIsExcel(FetchDataReportRequest request)
        throws IOException, BadRequestException {
        var testData = new TestData(generateDatePairs(2));
        loadModels(testData.getModels());
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var input = generateHandlerRequest(request);
        var output = executeRequest(handler, input);
        var expected = getExpectedExcel(request, testData);
        var decodedResponse = Base64.getDecoder().decode(fromOutputStream(output, String.class).getBody());
        var actual = new Excel(new XSSFWorkbook(new ByteArrayInputStream(decodedResponse)));
        assertEqualsInAnyOrder(expected, actual);
    }

    @ParameterizedTest
    @EnumSource(ReportType.class)
    void shouldReturnResultWithOffset(ReportType reportType) throws IOException {
        var testData = new TestData(generateDatePairs(2));
        loadModels(testData.getModels());
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var pageSize = 1;
        var firstRequest = generateHandlerRequest(buildRequest(OFFSET_ZERO, valueOf(pageSize), reportType.getType()));
        var firstOutput = executeRequest(handler, firstRequest);
        var firstRequestDataLines = extractDataLines(fromOutputStream(firstOutput, String.class).getBody());
        assertEquals(pageSize, firstRequestDataLines.size());
        var secondRequest = generateHandlerRequest(buildRequest(OFFSET_ONE, valueOf(pageSize), reportType.getType()));
        var secondOutput = executeRequest(handler, secondRequest);
        var secondRequestDataLines = extractDataLines(fromOutputStream(secondOutput, String.class).getBody());
        assertEquals(pageSize, secondRequestDataLines.size());
        assertNotEquals(firstRequestDataLines, secondRequestDataLines);
    }

    @Test
    void shouldRetrieveManyHits() throws IOException, BadRequestException {
        var testData = new TestData(generateDatePairs(20));
        loadModels(testData.getModels());
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var request = new FetchDataReportRequest(
            TEXT_CSV.toString(),
            "affiliation",
            "2026-01-01T03:02:11Z",
            "1998-01-01T05:09:32Z",
            "0",
            "100"
        );
        var input = generateHandlerRequest(request);
        var output = executeRequest(handler, input);
        var response = fromOutputStream(output, String.class);
        assertEquals(200, response.getStatusCode());
        var expected = getExpected(request, testData);
        var sortedResponse = sortResponse(ResultType.fromString(getReportFormat(request).toString()),
                                          response.getBody(), PUBLICATION_ID, CONTRIBUTOR_IDENTIFIER);
        assertEquals(expected, sortedResponse);
    }

    private static ByteArrayOutputStream executeRequest(FetchDataReport handler, InputStream inputStream)
        throws IOException {
        var output = new ByteArrayOutputStream();
        handler.handleRequest(inputStream, output, new FakeContext());
        return output;
    }

    private static FetchDataReportRequest buildRequest(String offset, String pageSize, String reportType) {
        return new FetchDataReportRequest(
            TEXT_PLAIN.toString(),
            reportType,
            LocalDate.now(ZoneId.systemDefault()).toString(),
            "1998-01-01",
            offset,
            pageSize
        );
    }

    private static ReportFormat getReportFormat(FetchDataReportRequest request) {
        return ReportFormat.fromMediaType(request.acceptHeader().get(ACCEPT));
    }

    private static InputStream generateHandlerRequest(FetchDataReportRequest request) throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(JsonUtils.dtoObjectMapper)
                   .withHeaders(request.acceptHeader())
                   .withPathParameters(request.pathParameters())
                   .withQueryParameters(request.queryParameters())
                   .build();
    }

    private static String getExpectedResponseData(FetchDataReportRequest request, TestData test)
        throws BadRequestException {
        return switch (ReportType.parse(request.reportType())) {
            case AFFILIATION -> test.getAffiliationResponseData();
            case CONTRIBUTOR -> test.getContributorResponseData();
            case FUNDING -> test.getFundingResponseData();
            case IDENTIFIER -> test.getIdentifierResponseData();
            case PUBLICATION -> test.getPublicationResponseData();
            case NVI -> test.getNviResponseData();
        };
    }

    // TODO: Craft queries and data to test every SELECT clause, BEFORE/AFTER/OFFSET/PAGE_SIZE.
    private String getExpected(FetchDataReportRequest request, TestData test) throws BadRequestException {
        var reportFormat = getReportFormat(request);
        var data = getExpectedResponseData(request, test);
        return ReportFormat.CSV.equals(reportFormat)
                   ? data
                   : generateTable(data);
    }

    private Excel getExpectedExcel(FetchDataReportRequest request, TestData test) throws BadRequestException {
        var data = getExpectedResponseData(request, test);
        return generateExcel(data);
    }
}

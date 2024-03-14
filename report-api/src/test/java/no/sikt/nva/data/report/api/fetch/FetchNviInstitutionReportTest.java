package no.sikt.nva.data.report.api.fetch;

import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedCsvFormatter.generateTable;
import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedExcelFormatter.generateExcel;
import static no.sikt.nva.data.report.api.fetch.formatter.ResultSorter.sortResponse;
import static no.sikt.nva.data.report.api.fetch.testutils.ExcelAsserter.assertEqualsInAnyOrder;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.Constants.organizationUri;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.TestData.SOME_TOP_LEVEL_IDENTIFIER;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.GatewayResponse.fromOutputStream;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.stream.Stream;
import no.sikt.nva.data.report.api.fetch.model.ReportFormat;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders;
import no.sikt.nva.data.report.api.fetch.testutils.generator.TestData;
import no.sikt.nva.data.report.api.fetch.testutils.requests.FetchNviInstitutionReportRequest;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.AccessRight;
import nva.commons.logutils.LogUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FetchNviInstitutionReportTest extends LocalFusekiTest {
    public static final String SOME_YEAR = "2023";
    public static final URI HARDCODED_INSTITUTION_ID = URI.create(organizationUri(SOME_TOP_LEVEL_IDENTIFIER));
    private FetchNviInstitutionReport handler;

    @BeforeEach
    void setUp() {
        handler = new FetchNviInstitutionReport(new QueryService(databaseConnection));
    }

    @Test
    void shouldExtractAndLogPathParameterReportingYear() throws IOException {
        var logAppender = LogUtils.getTestingAppenderForRootLogger();
        var request = generateHandlerRequest(new FetchNviInstitutionReportRequest(SOME_YEAR, randomUri()
            , "text/plain"));
        var output = new ByteArrayOutputStream();
        var context = new FakeContext();
        handler.handleRequest(request, output, context);
        assertTrue(logAppender.getMessages().contains("reporting year: " + SOME_YEAR));
    }

    @Test
    void shouldLogQueryParamInstitutionId() throws IOException {
        var logAppender = LogUtils.getTestingAppenderForRootLogger();
        var topLevelCristinOrgId = randomUri();
        var request = generateHandlerRequest(new FetchNviInstitutionReportRequest(SOME_YEAR, topLevelCristinOrgId,
                                                                                  "text/plain")
        );
        var output = new ByteArrayOutputStream();
        var context = new FakeContext();
        handler.handleRequest(request, output, context);
        assertTrue(logAppender.getMessages().contains("for organization: " + topLevelCristinOrgId));
    }

    @ParameterizedTest
    @MethodSource("fetchNviInstitutionReportRequestProvider")
    void shouldReturnFormattedResult(FetchNviInstitutionReportRequest request) throws IOException {
        var testData = new TestData(generateDatePairs(2));
        loadModels(testData.getModels());
        var output = new ByteArrayOutputStream();
        handler.handleRequest(generateHandlerRequest(request), output, new FakeContext());
        var response = fromOutputStream(output, String.class);
        assertEquals(200, response.getStatusCode());
        var expected = getExpected(request, testData);
        var sortedResponse = sortResponse(getReportFormat(request), response.getBody(),
                                          NviInstitutionStatusHeaders.PUBLICATION_IDENTIFIER,
                                          NviInstitutionStatusHeaders.CONTRIBUTOR_IDENTIFIER);
        assertEquals(expected, sortedResponse);
    }

    @ParameterizedTest
    @MethodSource("fetchNviInstitutionReportExcelRequestProvider")
    void shouldReturnBase64EncodedOutputStreamWhenContentTypeIsExcel(FetchNviInstitutionReportRequest request)
        throws IOException {
        var testData = new TestData(generateDatePairs(2));
        loadModels(testData.getModels());
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
        loadModels(testData.getModels());
        var output = new ByteArrayOutputStream();
        handler.handleRequest(generateHandlerRequest(request), output, new FakeContext());
        var expected = getExpectedExcel(testData);
        var decodedResponse = Base64.getDecoder().decode(fromOutputStream(output, String.class).getBody());
        var actual = new Excel(new XSSFWorkbook(new ByteArrayInputStream(decodedResponse)));
        assertEqualsInAnyOrder(expected, actual);
    }

    private static Stream<Arguments> fetchNviInstitutionReportRequestProvider() {
        return Stream.of(Arguments.of(new FetchNviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID, "text"
                                                                                                                +
                                                                                                                "/plain")),
                         Arguments.of(new FetchNviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID, "text"
                                                                                                                +
                                                                                                                "/csv"
                         )));
    }

    private static Stream<Arguments> fetchNviInstitutionReportExcelRequestProvider() {
        return Stream.of(Arguments.of(new FetchNviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID,
                                                                           "application/vnd.ms-excel")),
                         Arguments.of(new FetchNviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID,
                                                                           "application/vnd"
                                                                           + ".openxmlformats-officedocument"
                                                                           + ".spreadsheetml.sheet")));
    }

    private static InputStream generateHandlerRequest(FetchNviInstitutionReportRequest request)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(JsonUtils.dtoObjectMapper)
                   .withHeaders(request.acceptHeader())
                   .withPathParameters(request.pathParameters())
                   .withQueryParameters(request.queryParameters())
                   .build();
    }

    private static ReportFormat getReportFormat(FetchNviInstitutionReportRequest request) {
        return ReportFormat.fromMediaType(request.acceptHeader().get(ACCEPT));
    }

    private String getExpected(FetchNviInstitutionReportRequest request, TestData test) {
        var reportFormat = getReportFormat(request);
        var data = test.getNviInstitutionStatusResponseData(SOME_YEAR,
                                                            URI.create(organizationUri(SOME_TOP_LEVEL_IDENTIFIER)));
        return ReportFormat.CSV.equals(reportFormat)
                   ? data
                   : generateTable(data);
    }

    private Excel getExpectedExcel(TestData test) {
        return generateExcel(test.getNviInstitutionStatusResponseData(SOME_YEAR,
                                                                      URI.create(organizationUri(
                                                                          SOME_TOP_LEVEL_IDENTIFIER))));
    }
}

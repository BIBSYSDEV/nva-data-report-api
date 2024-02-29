package no.sikt.nva.data.report.api.fetch;

import static java.lang.String.valueOf;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedCsvFormatter.generateTable;
import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedExcelFormatter.generateExcel;
import static no.sikt.nva.data.report.api.fetch.formatter.ResultSorter.extractDataLines;
import static no.sikt.nva.data.report.api.fetch.formatter.ResultSorter.sortResponse;
import static no.sikt.nva.data.report.api.fetch.testutils.ExcelAsserter.assertEqualsInAnyOrder;
import static nva.commons.apigateway.GatewayResponse.fromOutputStream;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.fasterxml.jackson.core.JsonProcessingException;
import commons.db.DatabaseConnection;
import commons.db.GraphStoreProtocolConnection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.stream.IntStream;
import no.sikt.nva.data.report.api.fetch.model.ReportFormat;
import no.sikt.nva.data.report.api.fetch.model.ReportType;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import no.sikt.nva.data.report.api.fetch.testutils.BadRequestProvider;
import no.sikt.nva.data.report.api.fetch.testutils.TestingRequest;
import no.sikt.nva.data.report.api.fetch.testutils.ValidExcelRequestSource;
import no.sikt.nva.data.report.api.fetch.testutils.ValidRequestSource;
import no.sikt.nva.data.report.api.fetch.testutils.generator.TestData;
import no.sikt.nva.data.report.api.fetch.testutils.generator.TestData.DatePair;
import no.sikt.nva.data.report.api.fetch.testutils.generator.publication.PublicationDate;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;
import no.sikt.nva.data.report.testing.utils.FusekiTestingServer;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

class FetchDataReportTest {

    private static final String GSP_ENDPOINT = "/gsp";
    private static final URI GRAPH = URI.create("https://example.org/graph");
    private static final String OFFSET_ZERO = "0";
    private static final String OFFSET_ONE = "1";
    private static FusekiServer server;
    private static DatabaseConnection databaseConnection;

    @BeforeAll
    static void setup() {
        var dataSet = DatasetFactory.createTxnMem();
        server = FusekiTestingServer.init(dataSet, GSP_ENDPOINT);
        var url = server.serverURL();
        var queryPath = new Environment().readEnv("QUERY_PATH");
        databaseConnection = new GraphStoreProtocolConnection(url, url, queryPath);
    }

    @AfterAll
    static void tearDown() {
        server.stop();
    }

    @AfterEach
    void clearDatabase() {
        try {
            databaseConnection.delete(GRAPH);
        } catch (Exception e) {
            // Necessary to avoid case where we hve already deleted the graph
            catchExpectedExceptionsExceptHttpException(e);
        }
    }

    @ParameterizedTest()
    @DisplayName("Should throw BadRequestException when input is invalid")
    @ArgumentsSource(BadRequestProvider.class)
    void shouldThrowBadRequest(TestingRequest report)
        throws IOException {
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var output = executeRequest(handler, generateHandlerRequest(report));
        var response = fromOutputStream(output, String.class);
        assertEquals(400, response.getStatusCode());
    }

    @ParameterizedTest
    @ArgumentsSource(ValidRequestSource.class)
    void shouldReturnFormattedResult(TestingRequest request) throws IOException, BadRequestException {
        var testData = new TestData(generateDatePairs(2));
        databaseConnection.write(GRAPH, toTriples(testData.getModel()), Lang.NTRIPLES);
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var input = generateHandlerRequest(request);
        var output = executeRequest(handler, input);
        var response = fromOutputStream(output, String.class);
        assertEquals(200, response.getStatusCode());
        var expected = getExpected(request, testData);
        var sortedResponse = sortResponse(getReportFormat(request), response.getBody());
        assertEquals(expected, sortedResponse);
    }

    @ParameterizedTest
    @ArgumentsSource(ValidExcelRequestSource.class)
    void shouldReturnBase64EncodedOutputStreamWhenContentTypeIsExcel(TestingRequest request) throws IOException {
        var testData = new TestData(generateDatePairs(2));
        databaseConnection.write(GRAPH, toTriples(testData.getModel()), Lang.NTRIPLES);
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
    void shouldReturnDataInExcelSheetWhenContentTypeIsExcel(TestingRequest request)
        throws IOException, BadRequestException {
        var testData = new TestData(generateDatePairs(2));
        databaseConnection.write(GRAPH, toTriples(testData.getModel()), Lang.NTRIPLES);
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
    @EnumSource(value = ReportType.class, names = {"AFFILIATION", "CONTRIBUTOR", "FUNDING", "IDENTIFIER", "PUBLICATION",
        "NVI",})
    void shouldReturnResultWithOffset(ReportType reportType) throws IOException {
        var testData = new TestData(generateDatePairs(2));
        databaseConnection.write(GRAPH, toTriples(testData.getModel()), Lang.NTRIPLES);
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
        databaseConnection.write(GRAPH, toTriples(testData.getModel()), Lang.NTRIPLES);
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var request = new TestingRequest(
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
        var sortedResponse = sortResponse(getReportFormat(request), response.getBody());
        assertEquals(expected, sortedResponse);
    }

    private static ByteArrayOutputStream executeRequest(FetchDataReport handler, InputStream inputStream)
        throws IOException {
        var output = new ByteArrayOutputStream();
        handler.handleRequest(inputStream, output, new FakeContext());
        return output;
    }

    private static TestingRequest buildRequest(String offset, String pageSize, String reportType) {
        return new TestingRequest(
            TEXT_PLAIN.toString(),
            reportType,
            "2024-01-01",
            "1998-01-01",
            offset,
            pageSize
        );
    }

    private static ReportFormat getReportFormat(TestingRequest request) {
        return ReportFormat.fromMediaType(request.acceptHeader().get(ACCEPT));
    }

    private static InputStream generateHandlerRequest(TestingRequest request) throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(JsonUtils.dtoObjectMapper)
                   .withHeaders(request.acceptHeader())
                   .withPathParameters(request.pathParameters())
                   .withQueryParameters(request.queryParameters())
                   .build();
    }

    private static void catchExpectedExceptionsExceptHttpException(Exception e) {
        if (!(e instanceof HttpException)) {
            throw new RuntimeException(e);
        }
    }

    private static String getExpectedResponseData(TestingRequest request, TestData test) throws BadRequestException {
        return switch (ReportType.parse(request.reportType())) {
            case AFFILIATION -> test.getAffiliationResponseData();
            case CONTRIBUTOR -> test.getContributorResponseData();
            case FUNDING -> test.getFundingResponseData();
            case IDENTIFIER -> test.getIdentifierResponseData();
            case PUBLICATION -> test.getPublicationResponseData();
            case NVI -> test.getNviResponseData();
            case NVI_INSTITUTION_STATUS -> test.getNviInstitutionStatusResponseData();
        };
    }

    private List<DatePair> generateDatePairs(int numberOfDatePairs) {
        return IntStream.range(0, numberOfDatePairs)
                   .mapToObj(i -> new DatePair(new PublicationDate("2024", "02", "02"),
                                               Instant.now().minus(100, ChronoUnit.DAYS)))
                   .toList();
    }

    private String toTriples(Model model) {
        var stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, model, Lang.NTRIPLES);
        return stringWriter.toString();
    }

    // TODO: Craft queries and data to test every SELECT clause, BEFORE/AFTER/OFFSET/PAGE_SIZE.
    private String getExpected(TestingRequest request, TestData test) throws BadRequestException {
        var reportFormat = getReportFormat(request);
        var data = getExpectedResponseData(request, test);
        return ReportFormat.CSV.equals(reportFormat)
                   ? data
                   : generateTable(data);
    }

    private Excel getExpectedExcel(TestingRequest request, TestData test) throws BadRequestException {
        var data = getExpectedResponseData(request, test);
        return generateExcel(data);
    }
}

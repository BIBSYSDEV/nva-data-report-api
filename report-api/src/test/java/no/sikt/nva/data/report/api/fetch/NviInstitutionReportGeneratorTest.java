package no.sikt.nva.data.report.api.fetch;

import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedCsvFormatter.generateTable;
import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedExcelFormatter.generateExcel;
import static no.sikt.nva.data.report.api.fetch.formatter.ResultSorter.sortResponse;
import static no.sikt.nva.data.report.api.fetch.testutils.ExcelAsserter.assertEqualsInAnyOrder;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.Constants.organizationUri;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.TestData.SOME_TOP_LEVEL_IDENTIFIER;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;
import no.sikt.nva.data.report.api.fetch.model.ReportFormat;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders;
import no.sikt.nva.data.report.api.fetch.testutils.generator.TestData;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;
import no.unit.nva.s3.S3Driver;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.paths.UnixPath;
import nva.commons.logutils.LogUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NviInstitutionReportGeneratorTest extends LocalFusekiTest {

    public static final String SOME_YEAR = "2023";
    public static final URI HARDCODED_INSTITUTION_ID = URI.create(organizationUri(SOME_TOP_LEVEL_IDENTIFIER));
    public static final String TEXT_PLAIN = "text/plain";
    private S3Driver s3Driver;
    private NviInstitutionReportGenerator handler;

    @BeforeEach
    void setUp() {
        var s3Client = new FakeS3Client();
        s3Driver = new S3Driver(s3Client, "nvi-reports");
        handler = new NviInstitutionReportGenerator(new QueryService(databaseConnection));
    }

    @Test
    void shouldLogRequest() {
        var logAppender = LogUtils.getTestingAppenderForRootLogger();
        var fileName = randomString();
        var request = new NviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID, TEXT_PLAIN, fileName);
        var context = new FakeContext();
        handler.handleRequest(request, context);
        assertTrue(logAppender.getMessages().contains("for organization: " + HARDCODED_INSTITUTION_ID));
        assertTrue(logAppender.getMessages().contains("reporting year: " + SOME_YEAR));
    }

    @ParameterizedTest
    @MethodSource("nviInstitutionReportRequestProvider")
    void shouldWriteTextFileToS3(NviInstitutionReportRequest request) throws IOException {
        var testData = new TestData(generateDatePairs(2));
        loadModels(testData.getModels());
        handler.handleRequest(request, new FakeContext());
        var expected = getExpected(request, testData);
        var actual = s3Driver.getFile(UnixPath.of(request.presignedFileName()));
        var sortedResponse = sortResponse(getReportFormat(request), actual,
                                          NviInstitutionStatusHeaders.PUBLICATION_IDENTIFIER,
                                          NviInstitutionStatusHeaders.CONTRIBUTOR_IDENTIFIER);
        assertEquals(expected, sortedResponse);
    }

    @ParameterizedTest
    @MethodSource("nviInstitutionReportExcelRequestProvider")
    void shouldWriteExcelFileToS3(NviInstitutionReportRequest request)
        throws IOException {
        var testData = new TestData(generateDatePairs(2));
        loadModels(testData.getModels());
        handler.handleRequest(request, new FakeContext());
        var expected = getExpectedExcel(testData);
        var actual = s3Driver.getFile(UnixPath.of(request.presignedFileName()));
        var actualExcel = new Excel(new XSSFWorkbook(new ByteArrayInputStream(actual.getBytes())));
        assertEqualsInAnyOrder(expected, actualExcel);
    }

    private static Stream<Arguments> nviInstitutionReportRequestProvider() {
        return Stream.of(
            Arguments.of(
                new NviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID, TEXT_PLAIN, randomString())),
            Arguments.of(new NviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID, "text/csv",
                                                         randomString())));
    }

    private static Stream<Arguments> nviInstitutionReportExcelRequestProvider() {
        return Stream.of(Arguments.of(new NviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID,
                                                                      "application/vnd.ms-excel", randomString())),
                         Arguments.of(new NviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID,
                                                                      "application/vnd"
                                                                      + ".openxmlformats-officedocument"
                                                                      + ".spreadsheetml.sheet", randomString())));
    }

    private static ReportFormat getReportFormat(NviInstitutionReportRequest request) {
        return ReportFormat.fromMediaType(request.mediaType());
    }

    private String getExpected(NviInstitutionReportRequest request, TestData test) {
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

package no.sikt.nva.data.report.api.fetch;

import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedExcelFormatter.generateExcel;
import static no.sikt.nva.data.report.api.fetch.testutils.ExcelAsserter.assertEqualsInAnyOrder;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.Constants.organizationUri;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.TestData.SOME_TOP_LEVEL_IDENTIFIER;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import no.sikt.nva.data.report.api.fetch.testutils.generator.TestData;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.FakeS3Client;
import nva.commons.core.Environment;
import nva.commons.logutils.LogUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

public class NviInstitutionReportGeneratorTest extends LocalFusekiTest {

    public static final String SOME_YEAR = "2023";
    private static final URI HARDCODED_INSTITUTION_ID = URI.create(organizationUri(SOME_TOP_LEVEL_IDENTIFIER));
    private static final String EXCEL = "application/vnd.ms-excel";
    private static final Environment ENVIRONMENT = new Environment();
    private static final String bucketName = ENVIRONMENT.readEnv("NVI_REPORTS_BUCKET");
    private static final String pageSize = ENVIRONMENT.readEnv("GRAPH_DATABASE_PAGE_SIZE");
    private NviInstitutionReportGenerator handler;
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        s3Client = new FakeS3Client();
        handler = new NviInstitutionReportGenerator(new QueryService(databaseConnection), s3Client, ENVIRONMENT);
    }

    @Test
    void shouldLogRequest() {
        var logAppender = LogUtils.getTestingAppenderForRootLogger();
        var fileName = randomString();
        var request = sqsEventWithOneMessage(new NviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID,
                                                                             EXCEL,
                                                                             fileName));
        var context = new FakeContext();
        handler.handleRequest(request, context);
        assertTrue(logAppender.getMessages().contains("for organization: " + HARDCODED_INSTITUTION_ID));
        assertTrue(logAppender.getMessages().contains("reporting year: " + SOME_YEAR));
    }

    @ParameterizedTest
    @MethodSource("nviInstitutionReportExcelRequestProvider")
    void shouldWriteExcelFileToS3(NviInstitutionReportRequest request) throws IOException {
        var numberGreaterThanPageSize = Integer.parseInt(pageSize) + 1;
        var testData = new TestData(generateDatePairs(numberGreaterThanPageSize));
        loadModels(testData.getModels());
        handler.handleRequest(sqsEventWithOneMessage(request), new FakeContext());
        var expected = getExpectedExcel(testData);
        var actual = getActualPersistedExcel(request);
        assertEqualsInAnyOrder(expected, actual);
    }

    @Test
    void shouldWriteExcelFileWithErrorMessageIfGenerationFails() throws IOException {
        var queryService = Mockito.mock(QueryService.class);
        Mockito.when(queryService.getResult(Mockito.any(), Mockito.anyMap()))
            .thenThrow(new RuntimeException("Some error"));
        handler = new NviInstitutionReportGenerator(queryService, s3Client, new Environment());
        var request = new NviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID, EXCEL, randomString());
        handler.handleRequest(sqsEventWithOneMessage(request), new FakeContext());
        var expected = expectedErrorReport();
        var actual = getActualPersistedExcel(request);
        assertEqualsInAnyOrder(expected, actual);
    }

    private static Excel expectedErrorReport() {
        var workbook = new XSSFWorkbook();
        workbook.createSheet()
            .createRow(0)
            .createCell(0)
            .setCellValue("Unexpected error occurred. Please contact support.");
        return new Excel(workbook);
    }

    private static Stream<Arguments> nviInstitutionReportExcelRequestProvider() {
        return Stream.of(Arguments.of(new NviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID,
                                                                      "application/vnd.ms-excel", randomString())),
                         Arguments.of(new NviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID,
                                                                      "application/vnd"
                                                                      + ".openxmlformats-officedocument"
                                                                      + ".spreadsheetml.sheet", randomString())));
    }

    private SQSEvent sqsEventWithOneMessage(NviInstitutionReportRequest nviInstitutionReportRequest) {
        var event = new SQSEvent();
        var message = new SQSEvent.SQSMessage();
        message.setBody(nviInstitutionReportRequest.toJsonString());
        event.setRecords(List.of(message));
        return event;
    }

    private Excel getActualPersistedExcel(NviInstitutionReportRequest request) throws IOException {
        var persistedObject = s3Client.getObject(
            GetObjectRequest.builder().bucket(bucketName).key(request.presignedFileName()).build());
        return new Excel(new XSSFWorkbook(new ByteArrayInputStream(persistedObject.readAllBytes())));
    }

    private Excel getExpectedExcel(TestData test) {
        return generateExcel(test.getNviInstitutionStatusResponseData(SOME_YEAR,
                                                                      URI.create(organizationUri(
                                                                          SOME_TOP_LEVEL_IDENTIFIER))));
    }
}

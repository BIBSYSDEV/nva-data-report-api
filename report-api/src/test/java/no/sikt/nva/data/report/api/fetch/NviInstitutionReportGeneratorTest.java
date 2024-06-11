package no.sikt.nva.data.report.api.fetch;

import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedExcelFormatter.generateExcel;
import static no.sikt.nva.data.report.api.fetch.testutils.ExcelAsserter.assertEqualsInAnyOrder;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.Constants.organizationUri;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.TestData.SOME_TOP_LEVEL_IDENTIFIER;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

public class NviInstitutionReportGeneratorTest extends LocalFusekiTest {

    public static final String SOME_YEAR = "2023";
    public static final URI HARDCODED_INSTITUTION_ID = URI.create(organizationUri(SOME_TOP_LEVEL_IDENTIFIER));
    public static final String TEXT_PLAIN = "text/plain";
    private final String bucketName = new Environment().readEnv("NVI_REPORTS_BUCKET");
    private NviInstitutionReportGenerator handler;
    private S3Client s3Client;

    @BeforeEach
    void setUp() {
        s3Client = new FakeS3Client();
        handler = new NviInstitutionReportGenerator(new QueryService(databaseConnection), s3Client, new Environment());
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
    @MethodSource("nviInstitutionReportExcelRequestProvider")
    void shouldWriteExcelFileToS3(NviInstitutionReportRequest request) throws IOException {
        var testData = new TestData(generateDatePairs(10));
        loadModels(testData.getModels());
        handler.handleRequest(request, new FakeContext());
        var expected = getExpectedExcel(testData);
        var actual = getActualPersistedExcel(request);
        assertEqualsInAnyOrder(expected, actual);
    }

    private static Stream<Arguments> nviInstitutionReportExcelRequestProvider() {
        return Stream.of(Arguments.of(new NviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID,
                                                                      "application/vnd.ms-excel", randomString())),
                         Arguments.of(new NviInstitutionReportRequest(SOME_YEAR, HARDCODED_INSTITUTION_ID,
                                                                      "application/vnd"
                                                                      + ".openxmlformats-officedocument"
                                                                      + ".spreadsheetml.sheet", randomString())));
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

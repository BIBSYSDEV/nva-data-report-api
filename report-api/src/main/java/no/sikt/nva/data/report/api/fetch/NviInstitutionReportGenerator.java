package no.sikt.nva.data.report.api.fetch;

import static java.util.Objects.nonNull;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import commons.db.GraphStoreProtocolConnection;
import commons.formatter.ResponseFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import no.sikt.nva.data.report.api.fetch.formatter.CsvFormatter;
import no.sikt.nva.data.report.api.fetch.formatter.ExcelFormatter;
import no.sikt.nva.data.report.api.fetch.formatter.PlainTextFormatter;
import no.sikt.nva.data.report.api.fetch.model.ReportFormat;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class NviInstitutionReportGenerator implements RequestHandler<NviInstitutionReportRequest, String> {

    private static final Logger logger = LoggerFactory.getLogger(NviInstitutionReportGenerator.class);
    private static final String GRAPH_DATABASE_PAGE_SIZE = "GRAPH_DATABASE_PAGE_SIZE";
    private static final String BUCKET = "NVI_REPORTS_BUCKET";
    private static final String NVI_INSTITUTION_SPARQL = "nvi-institution-status";
    private static final String REPLACE_REPORTING_YEAR = "__REPLACE_WITH_REPORTING_YEAR__";
    private static final String REPLACE_TOP_LEVEL_ORG = "__REPLACE_WITH_TOP_LEVEL_ORGANIZATION__";
    private static final String PAGE_SIZE = "__PAGE_SIZE__";
    private static final String OFFSET = "__OFFSET__";
    private final QueryService queryService;
    private final S3Client s3Client;
    private final String reportingBucketName;
    private final String pageSize;

    @JacocoGenerated
    public NviInstitutionReportGenerator() {
        this(new QueryService(new GraphStoreProtocolConnection()), defaultS3Client(), new Environment());
    }

    public NviInstitutionReportGenerator(QueryService queryService, S3Client s3Client, Environment environment) {
        this.queryService = queryService;
        this.s3Client = s3Client;
        reportingBucketName = environment.readEnv(BUCKET);
        pageSize = environment.readEnv(GRAPH_DATABASE_PAGE_SIZE);
    }

    @Override
    public String handleRequest(NviInstitutionReportRequest request, Context context) {
        logRequest(request);
        var offset = 0;
        var reportingYear = request.reportingYear();
        var organization = String.valueOf(request.nviOrganization());
        var result = getResult(reportingYear, organization, pageSize, String.valueOf(offset));
        var excel = Excel.fromJava(result.getResultVars(), extractData(result));
        while (isNotEmpty(result)) {
            offset += Integer.parseInt(pageSize);
            result = getResult(reportingYear, organization, pageSize, String.valueOf(offset));
            excel.addData(extractData(result));
        }
        persistReportInS3(request, excel.toBytes());
        return null;
    }

    private static List<List<String>> extractData(ResultSet resultSet) {
        var data = new ArrayList<List<String>>();
        while (resultSet.hasNext()) {
            var row = resultSet.next();
            var rowData = new ArrayList<String>();
            for (String header : resultSet.getResultVars()) {
                var cell = row.get(header);
                rowData.add(nonNull(cell) ? cell.toString() : EMPTY_STRING);
            }
            data.add(rowData);
        }
        return data;
    }

    @JacocoGenerated
    private static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
    }

    private static void logRequest(NviInstitutionReportRequest request) {
        logger.info("NVI institution status report requested for organization: {}, reporting year: {}",
                    request.nviOrganization(), request.reportingYear());
    }

    private static ResponseFormatter getFormatter(ReportFormat reportFormat) {
        return switch (reportFormat) {
            case CSV -> new CsvFormatter();
            case EXCEL -> new ExcelFormatter();
            case TEXT -> new PlainTextFormatter();
        };
    }

    private boolean isNotEmpty(ResultSet result) {
        return result.getRowNumber() > 0;
    }

    private void persistReportInS3(NviInstitutionReportRequest request, byte[] bytes) {
        s3Client.putObject(buildRequest(request), RequestBody.fromBytes(bytes));
    }

    private PutObjectRequest buildRequest(NviInstitutionReportRequest request) {
        return PutObjectRequest.builder()
                   .bucket(reportingBucketName)
                   .key(request.presignedFileName())
                   .build();
    }

    private ResultSet getResult(String reportingYear, String topLevelOrganization,
                                String pageSize, String offset) {
        var replacementStrings = Map.of(REPLACE_REPORTING_YEAR, reportingYear,
                                        REPLACE_TOP_LEVEL_ORG, topLevelOrganization,
                                        PAGE_SIZE, pageSize,
                                        OFFSET, offset);
        return queryService.getResult(NVI_INSTITUTION_SPARQL, replacementStrings);
    }
}

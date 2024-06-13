package no.sikt.nva.data.report.api.fetch;

import static no.sikt.nva.data.report.api.fetch.utils.ExceptionUtils.getStackTrace;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.GLOBAL_STATUS;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.INTERNATIONAL_COLLABORATION_FACTOR;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.PUBLICATION_CHANNEL_LEVEL_POINTS;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.PUBLICATION_IDENTIFIER;
import static no.sikt.nva.data.report.api.fetch.utils.ResultUtil.extractData;
import static no.sikt.nva.data.report.api.fetch.utils.ResultUtil.isNotEmpty;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import commons.db.GraphStoreProtocolConnection;
import java.util.List;
import java.util.Map;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.attempt.Failure;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class NviInstitutionReportGenerator implements RequestHandler<SQSEvent, String> {

    private static final Logger logger = LoggerFactory.getLogger(NviInstitutionReportGenerator.class);
    private static final int PAGINATION_STARTING_OFFSET = 0;
    private static final String GRAPH_DATABASE_PAGE_SIZE = "GRAPH_DATABASE_PAGE_SIZE";
    private static final String BUCKET = "NVI_REPORTS_BUCKET";
    private static final String REPLACE_REPORTING_YEAR = "__REPLACE_WITH_REPORTING_YEAR__";
    private static final String REPLACE_TOP_LEVEL_ORG = "__REPLACE_WITH_TOP_LEVEL_ORGANIZATION__";
    private static final String PAGE_SIZE = "__PAGE_SIZE__";
    private static final String OFFSET = "__OFFSET__";
    private static final String NVI_INSTITUTION_SPARQL = "nvi-institution-status";
    private static final String FETCH_DATA_MESSAGE = "Fetching data with offset: {} and page size: {}";
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
    public String handleRequest(SQSEvent event, Context context) {
        var request = extractFirstRequest(event);
        logRequest(request);
        var report = attempt(() -> generateReport(request)).orElse(NviInstitutionReportGenerator::getFailureReport);
        persistReportInS3(request, report.toBytes());
        return null;
    }

    private static Excel getFailureReport(Failure<Excel> failure) {
        logger.error("Failure while generating report. Error: {}", getStackTrace(failure.getException()));
        return Excel.errorReport();
    }

    @JacocoGenerated
    private static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
    }

    private static void logRequest(NviInstitutionReportRequest request) {
        logger.info("NVI institution status report requested for organization: {}, reporting year: {}",
                    request.nviOrganization(), request.reportingYear());
    }

    private Excel generateReport(NviInstitutionReportRequest request) {
        var offset = PAGINATION_STARTING_OFFSET;
        var reportingYear = request.reportingYear();
        var organization = String.valueOf(request.nviOrganization());
        logger.info(FETCH_DATA_MESSAGE, offset, pageSize);
        var result = getResult(reportingYear, organization, pageSize, String.valueOf(offset));
        var report = Excel.fromJava(result.getResultVars(), extractData(result));
        while (isNotEmpty(result)) {
            offset += Integer.parseInt(pageSize);
            logger.info(FETCH_DATA_MESSAGE, offset, pageSize);
            result = getResult(reportingYear, organization, pageSize, String.valueOf(offset));
            report.addData(extractData(result));
        }
        return report.postProcess(
            List.of(GLOBAL_STATUS,
                    INTERNATIONAL_COLLABORATION_FACTOR,
                    PUBLICATION_CHANNEL_LEVEL_POINTS,
                    PUBLICATION_IDENTIFIER,
                    CONTRIBUTOR_IDENTIFIER));
    }

    private NviInstitutionReportRequest extractFirstRequest(SQSEvent input) {
        return attempt(() -> NviInstitutionReportRequest.from(extractFirstMessage(input).getBody())).orElseThrow();
    }

    private SQSMessage extractFirstMessage(SQSEvent input) {
        return input.getRecords().stream().findFirst().orElseThrow();
    }

    private void persistReportInS3(NviInstitutionReportRequest request, byte[] bytes) {
        logger.info("Persisting report for organization: {} and reporting year: {} in S3 bucket",
                    request.nviOrganization(), request.reportingYear());
        s3Client.putObject(buildRequest(request), RequestBody.fromBytes(bytes));
        logger.info("Report persisted in S3 bucket");
    }

    private PutObjectRequest buildRequest(NviInstitutionReportRequest request) {
        return PutObjectRequest.builder()
                   .bucket(reportingBucketName)
                   .contentType(request.mediaType())
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

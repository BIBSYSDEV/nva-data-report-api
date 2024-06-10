package no.sikt.nva.data.report.api.fetch;

import static java.nio.charset.StandardCharsets.UTF_8;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import commons.db.GraphStoreProtocolConnection;
import commons.formatter.ResponseFormatter;
import java.net.URLDecoder;
import java.util.Map;
import no.sikt.nva.data.report.api.fetch.formatter.CsvFormatter;
import no.sikt.nva.data.report.api.fetch.formatter.ExcelFormatter;
import no.sikt.nva.data.report.api.fetch.formatter.PlainTextFormatter;
import no.sikt.nva.data.report.api.fetch.model.ReportFormat;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import no.unit.nva.s3.S3Driver;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;

public class NviInstitutionReportGenerator implements RequestHandler<NviInstitutionReportRequest, String> {

    private static final Logger logger = LoggerFactory.getLogger(NviInstitutionReportGenerator.class);
    private static final String ACCEPT = "Accept";
    private static final String QUERY_PARAM_INSTITUTION_ID = "institutionId";
    private static final String QUERY_PARAM_REPORTING_YEAR = "reportingYear";
    private static final String NVI_INSTITUTION_SPARQL = "nvi-institution-status";
    private static final String REPLACE_REPORTING_YEAR = "__REPLACE_WITH_REPORTING_YEAR__";
    private static final String REPLACE_TOP_LEVEL_ORG = "__REPLACE_WITH_TOP_LEVEL_ORGANIZATION__";
    private final QueryService queryService;
    private final S3Client s3Client;

    @JacocoGenerated
    public NviInstitutionReportGenerator() {
        this(new QueryService(new GraphStoreProtocolConnection()), defaultS3Client());
    }

    public NviInstitutionReportGenerator(QueryService queryService, S3Client s3Client) {
        this.queryService = queryService;
        this.s3Client = s3Client;
    }

    @Override
    public String handleRequest(NviInstitutionReportRequest request, Context context) {
        return null;
    }

    protected String processInput(Void unused, RequestInfo requestInfo, Context context) throws BadRequestException {
        var reportingYear = requestInfo.getQueryParameter(QUERY_PARAM_REPORTING_YEAR);
        var topLevelOrganization =
            URLDecoder.decode(requestInfo.getQueryParameter(QUERY_PARAM_INSTITUTION_ID), UTF_8);
        logRequest(topLevelOrganization, reportingYear);
        var reportFormat = ReportFormat.fromMediaType(requestInfo.getHeader(ACCEPT));
        var result = getResult(reportingYear, topLevelOrganization, reportFormat);
        return result;
    }

    @JacocoGenerated
    private static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
    }

    private static void logRequest(String topLevelOrganization, String reportingYear) {
        logger.info("NVI institution status report requested for organization: {}, reporting year: {}",
                    topLevelOrganization, reportingYear);
    }

    private static ResponseFormatter getFormatter(ReportFormat reportFormat) {
        return switch (reportFormat) {
            case CSV -> new CsvFormatter();
            case EXCEL -> new ExcelFormatter();
            case TEXT -> new PlainTextFormatter();
        };
    }

    private String getResult(String reportingYear, String topLevelOrganization, ReportFormat reportFormat) {
        var replacementStrings = Map.of(REPLACE_REPORTING_YEAR, reportingYear,
                                        REPLACE_TOP_LEVEL_ORG, topLevelOrganization);
        return queryService.getResult(NVI_INSTITUTION_SPARQL, replacementStrings, getFormatter(reportFormat));
    }
}

package no.sikt.nva.data.report.api.fetch;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static com.google.common.net.MediaType.OOXML_SHEET;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.aws.AwsSqsClient;
import no.sikt.nva.data.report.api.fetch.queue.QueueClient;
import nva.commons.apigateway.ApiS3PresignerGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;

public class FetchNviInstitutionReportProxy extends ApiS3PresignerGatewayHandler<Void> {

    public static final String NVI_REPORTS_BUCKET = "NVI_REPORTS_BUCKET";
    private static final Logger logger = LoggerFactory.getLogger(FetchNviInstitutionReport.class);
    private static final String ACCEPT_HEADER = "Accept";
    private static final String PATH_PARAMETER_REPORTING_YEAR = "reportingYear";
    private static final Duration SIGN_DURATION = Duration.ofMinutes(60);
    private static final String REGION = "AWS_REGION_NAME";
    private static final String QUEUE_URL = "REPORT_QUEUE_URL";
    private final QueueClient queueClient;

    @JacocoGenerated
    public FetchNviInstitutionReportProxy() {
        this(new AwsSqsClient(Region.of(new Environment().readEnv(REGION)),
                              new Environment().readEnv(QUEUE_URL)));
    }

    public FetchNviInstitutionReportProxy(QueueClient queueClient) {
        super(Void.class, ApiS3PresignerGatewayHandler.defaultS3Presigner());
        this.queueClient = queueClient;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return List.of(TEXT_CSV, TEXT_PLAIN, MICROSOFT_EXCEL, OOXML_SHEET);
    }

    @Override
    protected void generateAndWriteDataToS3(String s, Void unused, RequestInfo requestInfo, Context context) {
        validateAccessRights(requestInfo);
        var reportingYear = requestInfo.getPathParameter(PATH_PARAMETER_REPORTING_YEAR);
        var topLevelOrganization = extractTopLevelOrganization(requestInfo);
        var acceptHeader = requestInfo.getHeader(ACCEPT_HEADER);
        logRequest(topLevelOrganization, reportingYear);
        sendEvent(reportingYear, topLevelOrganization, acceptHeader);
    }

    @Override
    protected String getBucketName() {
        return new Environment().readEnv(NVI_REPORTS_BUCKET);
    }

    @Override
    protected Duration getSignDuration() {
        return SIGN_DURATION;
    }

    private static String extractTopLevelOrganization(RequestInfo requestInfo) {
        return requestInfo.getTopLevelOrgCristinId()
                   .map(URI::toString)
                   .orElse("UnknownRequestTopLevelOrganization");
    }

    private static void logRequest(String topLevelOrganization, String reportingYear) {
        logger.info("NVI institution status report requested for organization: {}, reporting year: {}",
                    topLevelOrganization, reportingYear);
    }

    private void sendEvent(String reportingYear, String topLevelOrganization, String mediaType) {
        //TODO
    }

    private void validateAccessRights(RequestInfo requestInfo) {
        //TODO
        //if (!requestInfo.userIsAuthorized(AccessRight.MANAGE_NVI_CANDIDATES)) {
        //    throw new UnauthorizedException();
        //}
    }
}

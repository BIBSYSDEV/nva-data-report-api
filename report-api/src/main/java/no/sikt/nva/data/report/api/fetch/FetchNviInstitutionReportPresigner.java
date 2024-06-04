package no.sikt.nva.data.report.api.fetch;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static com.google.common.net.MediaType.OOXML_SHEET;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.time.Duration;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.aws.AwsSqsClient;
import no.sikt.nva.data.report.api.fetch.queue.QueueClient;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.ApiS3PresignerGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;

public class FetchNviInstitutionReportPresigner extends ApiS3PresignerGatewayHandler<Void> {

    public static final String NVI_REPORTS_BUCKET = "NVI_REPORTS_BUCKET";
    private static final Logger logger = LoggerFactory.getLogger(FetchNviInstitutionReport.class);
    private static final Duration SIGN_DURATION = Duration.ofMinutes(60);
    private static final String REGION = "AWS_REGION";
    private static final String QUEUE_URL = "REPORT_QUEUE_URL";
    private final QueueClient queueClient;

    @JacocoGenerated
    public FetchNviInstitutionReportPresigner() {
        this(new AwsSqsClient(Region.of(new Environment().readEnv(REGION)),
                              new Environment().readEnv(QUEUE_URL)));
    }

    public FetchNviInstitutionReportPresigner(QueueClient queueClient) {
        super(Void.class, ApiS3PresignerGatewayHandler.defaultS3Presigner());
        this.queueClient = queueClient;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return List.of(TEXT_CSV, TEXT_PLAIN, MICROSOFT_EXCEL, OOXML_SHEET);
    }

    @Override
    protected void validateRequest(Void unused, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateAccessRights(requestInfo);
    }

    @Override
    protected void generateAndWriteDataToS3(String fileName, Void unused, RequestInfo requestInfo, Context context) {
        var reportRequest = NviInstitutionReportRequest.from(requestInfo, fileName);
        logRequest(reportRequest);
        sendMessage(reportRequest);
    }

    @Override
    protected String getBucketName() {
        return new Environment().readEnv(NVI_REPORTS_BUCKET);
    }

    @Override
    protected Duration getSignDuration() {
        return SIGN_DURATION;
    }

    private static void logRequest(NviInstitutionReportRequest request) {
        logger.info("NVI institution status report requested for organization: {}, reporting year: {}",
                    request.nviOrganization(), request.reportingYear());
    }

    private void sendMessage(NviInstitutionReportRequest nviInstitutionReportRequest) {
        queueClient.sendMessage(nviInstitutionReportRequest.toJsonString());
        logger.info("Message sent to queue: {}", nviInstitutionReportRequest.toJsonString());
    }

    private void validateAccessRights(RequestInfo requestInfo) throws UnauthorizedException {
        if (!requestInfo.userIsAuthorized(AccessRight.MANAGE_NVI_CANDIDATES)) {
            throw new UnauthorizedException();
        }
    }
}

package no.sikt.nva.data.report.api.fetch;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static com.google.common.net.MediaType.OOXML_SHEET;
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
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public class FetchNviInstitutionReportPresigner extends ApiS3PresignerGatewayHandler<Void> {

    private static final Logger logger = LoggerFactory.getLogger(FetchNviInstitutionReportPresigner.class);
    private static final String SIGN_DURATION = "SIGN_DURATION_IN_MINUTES";
    private static final String REGION = "AWS_REGION";
    private static final String QUEUE_URL = "REPORT_QUEUE_URL";
    private static final String NVI_REPORTS_BUCKET = "NVI_REPORTS_BUCKET";
    private final QueueClient queueClient;
    private final Duration signDuration;

    @JacocoGenerated
    public FetchNviInstitutionReportPresigner() {
        this(new AwsSqsClient(Region.of(new Environment().readEnv(REGION)), new Environment().readEnv(QUEUE_URL)),
             ApiS3PresignerGatewayHandler.defaultS3Presigner(),
             Duration.ofMinutes(Integer.parseInt(new Environment().readEnv(SIGN_DURATION))));
    }

    public FetchNviInstitutionReportPresigner(QueueClient queueClient, S3Presigner s3Presigner, Duration signDuration) {
        super(Void.class, s3Presigner);
        this.queueClient = queueClient;
        this.signDuration = signDuration;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return List.of(MICROSOFT_EXCEL, OOXML_SHEET);
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
        return signDuration;
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

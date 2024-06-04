package no.sikt.nva.data.report.api.fetch;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.AccessRight.MANAGE_NVI_CANDIDATES;
import static nva.commons.apigateway.GatewayResponse.fromOutputStream;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import no.sikt.nva.data.report.api.fetch.testutils.requests.FetchNviInstitutionReportProxyRequest;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

public class FetchNviInstitutionReportHandlerPresignerTest {

    private static final String TEXT_PLAIN = "text/plain";
    private static final String TEXT_CSV = "text/csv";
    private static final String OPEN_XML = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String EXCEL = "application/vnd.ms-excel";
    private static final String SOME_YEAR = "2023";
    private static final AccessRight SOME_ACCESS_RIGHT_THAT_IS_NOT_MANAGE_NVI = AccessRight.SUPPORT;
    private FetchNviInstitutionReportPresigner handler;
    private FakeSqsClient queueClient;
    private S3Presigner mockedS3Presigner;

    @BeforeEach
    public void setup() {
        queueClient = new FakeSqsClient();
        mockedS3Presigner = mock(S3Presigner.class);
        handler = new FetchNviInstitutionReportPresigner(queueClient, mockedS3Presigner);
    }

    @Test
    void shouldReturn401WhenUserDoesNotHaveManageNviAccessRight() throws IOException {
        var request = new FetchNviInstitutionReportProxyRequest(SOME_YEAR, TEXT_PLAIN);
        var unAuthorizedRequest = generateHandlerRequest(request, SOME_ACCESS_RIGHT_THAT_IS_NOT_MANAGE_NVI,
                                                         randomUri());
        var output = new ByteArrayOutputStream();
        var context = new FakeContext();
        handler.handleRequest(unAuthorizedRequest, output, context);
        var response = fromOutputStream(output, GatewayResponse.class);
        var actualProblem = objectMapper.readValue(response.getBody(), Problem.class);
        var expectedProblem = getExpectedProblem(context.getAwsRequestId());
        assertEquals(expectedProblem, objectMapper.writeValueAsString(actualProblem));
    }

    @Test
    void shouldLogRequestingUsersTopLevelOrganization() throws IOException {
        var logAppender = LogUtils.getTestingAppenderForRootLogger();
        var institutionId = randomUri();
        var request = generateHandlerRequest(new FetchNviInstitutionReportProxyRequest(SOME_YEAR, TEXT_PLAIN),
                                             institutionId);
        var output = new ByteArrayOutputStream();
        var context = new FakeContext();
        handler.handleRequest(request, output, context);
        assertTrue(logAppender.getMessages().contains("for organization: " + institutionId));
    }

    @Test
    void shouldExtractAndLogPathParameterReportingYear() throws IOException {
        var logAppender = LogUtils.getTestingAppenderForRootLogger();
        var request = generateHandlerRequest(new FetchNviInstitutionReportProxyRequest(SOME_YEAR, TEXT_PLAIN),
                                             randomUri());
        var output = new ByteArrayOutputStream();
        var context = new FakeContext();
        handler.handleRequest(request, output, context);
        assertTrue(logAppender.getMessages().contains("reporting year: " + SOME_YEAR));
    }

    @ParameterizedTest
    @ValueSource(strings = {TEXT_CSV, TEXT_PLAIN, OPEN_XML, EXCEL})
    void shouldReturnPreSignedUrl(String contentType) throws IOException {
        var output = new ByteArrayOutputStream();
        var request = new FetchNviInstitutionReportProxyRequest(SOME_YEAR, contentType);
        var context = new FakeContext();
        var presignedGetObjectRequest = mockPresignResponse(context.getAwsRequestId());
        when(mockedS3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
            .thenReturn(presignedGetObjectRequest);
        handler.handleRequest(generateHandlerRequest(request, randomUri()), output, context);
        var response = fromOutputStream(output, GatewayResponse.class);
        assertTrue(response.getHeaders().get("Location").contains(context.getAwsRequestId()));
        assertEquals(HTTP_MOVED_TEMP, response.getStatusCode());
    }

    @Test
    void shouldSendMessageWithGenerateNviReportRequest() throws IOException {
        var output = new ByteArrayOutputStream();
        var request = new FetchNviInstitutionReportProxyRequest(SOME_YEAR, TEXT_CSV);
        var context = new FakeContext();
        var topLevelCristinOrgId = randomUri();
        handler.handleRequest(generateHandlerRequest(request, topLevelCristinOrgId), output, context);
        var expectedSentRequest = new NviInstitutionReportRequest(SOME_YEAR, topLevelCristinOrgId, TEXT_CSV,
                                                                  context.getAwsRequestId());
        var actualSentRequest = dtoObjectMapper.readValue(queueClient.getSentMessages().getFirst().messageBody(),
                                                          NviInstitutionReportRequest.class);
        assertEquals(expectedSentRequest, actualSentRequest);
    }

    private static PresignedGetObjectRequest mockPresignResponse(String filename) throws MalformedURLException {
        var presignRequest = mock(PresignedGetObjectRequest.class);
        var presignedUrl = "https://example.com/" + filename;
        when(presignRequest.url()).thenReturn(new URL(presignedUrl));
        return presignRequest;
    }

    private static InputStream generateHandlerRequest(FetchNviInstitutionReportProxyRequest request,
                                                      URI topLevelCristinOrgId)
        throws JsonProcessingException {
        return generateHandlerRequest(request, MANAGE_NVI_CANDIDATES, topLevelCristinOrgId);
    }

    private static InputStream generateHandlerRequest(FetchNviInstitutionReportProxyRequest request,
                                                      AccessRight accessRight,
                                                      URI topLevelCristinOrgId)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(dtoObjectMapper)
                   .withHeaders(request.acceptHeader())
                   .withAccessRights(randomUri(), accessRight)
                   .withPathParameters(request.pathParameters())
                   .withTopLevelCristinOrgId(topLevelCristinOrgId)
                   .build();
    }

    private static String getExpectedProblem(String requestId) {
        return attempt(() -> objectMapper.writeValueAsString(Problem.builder()
                                                                 .withStatus(Status.UNAUTHORIZED)
                                                                 .withTitle("Unauthorized")
                                                                 .withDetail("Unauthorized")
                                                                 .with("requestId", requestId)
                                                                 .build())).orElseThrow();
    }
}

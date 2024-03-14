package no.sikt.nva.data.report.api.fetch;

import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.GatewayResponse.fromOutputStream;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import no.sikt.nva.data.report.api.fetch.testutils.requests.FetchNviInstitutionReportProxyRequest;
import no.sikt.nva.data.report.api.fetch.testutils.requests.FetchNviInstitutionReportRequest;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class FetchNviInstitutionReportHandlerProxyTest {

    private static final String SOME_YEAR = "2023";
    private static final AccessRight SOME_ACCESS_RIGHT_THAT_IS_NOT_MANAGE_NVI = AccessRight.SUPPORT;
    private FetchNviInstitutionReportProxy handler;

    @BeforeEach
    public void setup() {
        handler = new FetchNviInstitutionReportProxy();
    }

    @Test
    void shouldReturn401WhenUserDoesNotHaveManageNviAccessRight() throws IOException {
        var request = new FetchNviInstitutionReportProxyRequest(SOME_YEAR, "text/plain");
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
        var topLevelCristinOrgId = randomUri();
        var request = generateHandlerRequest(new FetchNviInstitutionReportProxyRequest(SOME_YEAR, "text/plain"),
                                             AccessRight.MANAGE_NVI, topLevelCristinOrgId);
        var output = new ByteArrayOutputStream();
        var context = new FakeContext();
        handler.handleRequest(request, output, context);
        assertTrue(logAppender.getMessages().contains("for organization: " + topLevelCristinOrgId));
    }

    @Test
    void shouldExtractAndLogPathParameterReportingYear() throws IOException {
        var logAppender = LogUtils.getTestingAppenderForRootLogger();
        var request = generateHandlerRequest(new FetchNviInstitutionReportProxyRequest(SOME_YEAR, "text/plain"),
                                             AccessRight.MANAGE_NVI, randomUri());
        var output = new ByteArrayOutputStream();
        var context = new FakeContext();
        handler.handleRequest(request, output, context);
        assertTrue(logAppender.getMessages().contains("reporting year: " + SOME_YEAR));
    }

    private static InputStream generateHandlerRequest(FetchNviInstitutionReportProxyRequest request,
                                                      AccessRight accessRight,
                                                      URI topLevelCristinOrgId)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(JsonUtils.dtoObjectMapper)
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

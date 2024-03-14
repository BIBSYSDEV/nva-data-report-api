package no.sikt.nva.data.report.api.fetch;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.GatewayResponse.fromOutputStream;
import static nva.commons.core.attempt.Try.attempt;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import no.sikt.nva.data.report.api.fetch.testutils.requests.FetchNviInstitutionReportProxyRequest;
import no.unit.nva.auth.AuthorizedBackendClient;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class FetchNviInstitutionReportHandlerProxyTest {

    private static final String TEXT_PLAIN = "text/plain";
    private static final String QUERY_PARAM_REPORTING_YEAR = "reportingYear";
    private static final String QUERY_PARAM_INSTITUTION_ID = "institutionId";
    private static final String CUSTOM_DOMAIN_PATH = "report";
    private static final String INSTITUTION_PATH = "institution";
    private static final String NVI_APPROVAL_REPORT_PATH = "nvi-approval";
    private static final String TEXT_CSV = "text/csv";
    private static final String OPEN_XML = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String EXCEL = "vnd.ms-excel";
    private static final String API_HOST = new Environment().readEnv("API_HOST");
    private static final String SOME_YEAR = "2023";
    private static final AccessRight SOME_ACCESS_RIGHT_THAT_IS_NOT_MANAGE_NVI = AccessRight.SUPPORT;
    private FetchNviInstitutionReportProxy handler;
    private AuthorizedBackendClient authorizedBackendClient;

    @BeforeEach
    public void setup() {
        authorizedBackendClient = mock(AuthorizedBackendClient.class);
        handler = new FetchNviInstitutionReportProxy(authorizedBackendClient);
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
                                             AccessRight.MANAGE_NVI, institutionId);
        var output = new ByteArrayOutputStream();
        var context = new FakeContext();
        handler.handleRequest(request, output, context);
        assertTrue(logAppender.getMessages().contains("for organization: " + institutionId));
    }

    @Test
    void shouldExtractAndLogPathParameterReportingYear() throws IOException {
        var logAppender = LogUtils.getTestingAppenderForRootLogger();
        var request = generateHandlerRequest(new FetchNviInstitutionReportProxyRequest(SOME_YEAR, TEXT_PLAIN),
                                             AccessRight.MANAGE_NVI, randomUri());
        var output = new ByteArrayOutputStream();
        var context = new FakeContext();
        handler.handleRequest(request, output, context);
        assertTrue(logAppender.getMessages().contains("reporting year: " + SOME_YEAR));
    }

    @ParameterizedTest
    @ValueSource(strings = {TEXT_CSV, TEXT_PLAIN, OPEN_XML, EXCEL})
    void shouldReturnExpectedContentType(String contentType) throws IOException, InterruptedException {
        var request = new FetchNviInstitutionReportProxyRequest(SOME_YEAR, contentType);
        var institutionId = randomUri();
        var expectedResponseBody = randomString();
        mockResponse(institutionId, expectedResponseBody, contentType);
        var output = new ByteArrayOutputStream();
        var handlerRequest = generateHandlerRequest(request, AccessRight.MANAGE_NVI, institutionId);
        handler.handleRequest(handlerRequest, output, new FakeContext());
        var response = fromOutputStream(output, GatewayResponse.class);
        assertEquals(contentType, response.getHeaders().get("Content-Type"));
        assertEquals(HTTP_OK, response.getStatusCode());
        assertEquals(expectedResponseBody, response.getBody());
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

    private void mockResponse(URI institutionId, String responseBody, String contentType)
        throws IOException, InterruptedException {
        var uri = constructInstitutionReportEndpoint(institutionId.toString());
        var response = mockHttpResponse(responseBody);
        mockResponse(uri, response, contentType);
    }

    private void mockResponse(URI uri, HttpResponse<String> response, String contentType)
        throws IOException, InterruptedException {
        var request = createExpectedRequest(uri, contentType);
        when(
            authorizedBackendClient.send(eq(request), eq(BodyHandlers.ofString(UTF_8))))
            .thenReturn(response);
    }

    private URI constructInstitutionReportEndpoint(String institutionId) {
        return UriWrapper.fromHost(API_HOST)
                   .addChild(CUSTOM_DOMAIN_PATH)
                   .addChild(INSTITUTION_PATH)
                   .addChild(NVI_APPROVAL_REPORT_PATH)
                   .addQueryParameter(QUERY_PARAM_REPORTING_YEAR, SOME_YEAR)
                   .addQueryParameter(QUERY_PARAM_INSTITUTION_ID, URLEncoder.encode(institutionId, UTF_8))
                   .getUri();
    }

    private HttpResponse<String> mockHttpResponse(String expectedResponse) {
        var response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(HTTP_OK);
        when(response.body()).thenReturn(expectedResponse);
        return response;
    }

    private Builder createExpectedRequest(URI uri, String accept) {
        return HttpRequest.newBuilder(uri)
                   .header(ACCEPT, accept)
                   .GET();
    }
}

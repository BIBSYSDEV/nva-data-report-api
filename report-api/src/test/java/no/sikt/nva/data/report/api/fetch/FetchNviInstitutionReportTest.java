package no.sikt.nva.data.report.api.fetch;

import static nva.commons.apigateway.GatewayResponse.fromOutputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import no.sikt.nva.data.report.api.fetch.testutils.requests.FetchNviInstitutionReportRequest;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class FetchNviInstitutionReportTest extends LocalFusekiTest {

    @ParameterizedTest
    @ValueSource(strings = {"text/plain", "text/csv", "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"})
    void shouldHandleRequest(String contentType) throws IOException {
        var handler = new FetchNviInstitutionReport();
        var output = new ByteArrayOutputStream();
        var request = new FetchNviInstitutionReportRequest(contentType);
        var input = generateHandlerRequest(request);
        handler.handleRequest(input, output, new FakeContext());
        var response = fromOutputStream(output, String.class);
        assertEquals(200, response.getStatusCode());
    }

    private static InputStream generateHandlerRequest(FetchNviInstitutionReportRequest request)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(JsonUtils.dtoObjectMapper)
                   .withHeaders(request.acceptHeader())
                   .build();
    }
}

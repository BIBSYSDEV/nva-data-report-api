package no.sikt.nva.data.report.api.fetch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import org.junit.jupiter.api.Test;

class FetchDataReportTest {

    @Test
    void shouldExist() throws IOException {
        var handler = new FetchDataReport();
        var input = new HandlerRequestBuilder<InputStream>(JsonUtils.dtoObjectMapper).build();
        var output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, new FakeContext());
        var response = GatewayResponse.fromOutputStream(output, String.class);
        assertEquals(200, response.getStatusCode());
    }
}

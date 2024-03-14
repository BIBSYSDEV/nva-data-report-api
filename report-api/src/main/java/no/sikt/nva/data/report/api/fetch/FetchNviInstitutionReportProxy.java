package no.sikt.nva.data.report.api.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class FetchNviInstitutionReportProxy extends ApiGatewayHandler<Void, String> {

    public FetchNviInstitutionReportProxy() {
        super(Void.class);
    }

    @Override
    protected String processInput(Void unused, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        return null;
    }

    @Override
    protected Integer getSuccessStatusCode(Void unused, String o) {
        return HttpStatus.SC_OK;
    }
}

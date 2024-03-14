package no.sikt.nva.data.report.api.fetch;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static com.google.common.net.MediaType.OOXML_SHEET;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.util.List;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import org.apache.http.HttpStatus;

public class FetchNviInstitutionReportProxy extends ApiGatewayHandler<Void, String> {

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return List.of(TEXT_CSV, TEXT_PLAIN, MICROSOFT_EXCEL, OOXML_SHEET);
    }

    public FetchNviInstitutionReportProxy() {
        super(Void.class);
    }

    @Override
    protected String processInput(Void unused, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateAccessRights(requestInfo);
        return null;
    }

    private void validateAccessRights(RequestInfo requestInfo) throws UnauthorizedException {
        if (!requestInfo.userIsAuthorized(AccessRight.MANAGE_NVI)) {
            throw new UnauthorizedException();
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void unused, String o) {
        return HttpStatus.SC_OK;
    }
}

package no.sikt.nva.data.report.api.fetch;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static com.google.common.net.MediaType.OOXML_SHEET;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.util.List;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;

public class FetchNviInstitutionReport extends ApiGatewayHandler<Void, String> {

    public FetchNviInstitutionReport() {
        super(Void.class);
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return List.of(TEXT_CSV, TEXT_PLAIN, MICROSOFT_EXCEL, OOXML_SHEET);
    }

    @Override
    protected String processInput(Void unused, RequestInfo requestInfo, Context context) {
        return null;
    }

    @Override
    protected Integer getSuccessStatusCode(Void unused, String o) {
        return 200;
    }
}

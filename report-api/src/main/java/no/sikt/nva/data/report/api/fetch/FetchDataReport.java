package no.sikt.nva.data.report.api.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.db.NeptuneConnection;
import no.sikt.nva.data.report.api.fetch.formatter.CsvFormatter;
import no.sikt.nva.data.report.api.fetch.formatter.PlainTextFormatter;
import no.sikt.nva.data.report.api.fetch.model.ReportFormat;
import no.sikt.nva.data.report.api.fetch.model.ReportRequest;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.JacocoGenerated;

public class FetchDataReport extends ApiGatewayHandler<Void, String> {

    private static final String TEXT = "text";
    public static final MediaType TEXT_PLAIN = MediaType.create(TEXT, "plain");
    public static final String CSV = "csv";
    public static final MediaType TEXT_CSV = MediaType.create(TEXT, CSV);
    private final QueryService queryService;

    @JacocoGenerated
    public FetchDataReport() {
        this(new QueryService(new NeptuneConnection()));
    }

    public FetchDataReport(QueryService queryService) {
        super(Void.class);
        this.queryService = queryService;
    }

    @Override
    protected String processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        var reportRequest = ReportRequest.fromRequestInfo(requestInfo);
        var formatter = reportRequest.getReportFormat().equals(ReportFormat.CSV)
            ? new CsvFormatter() : new PlainTextFormatter();
        return queryService.getResult(reportRequest, formatter);
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return List.of(TEXT_CSV, TEXT_PLAIN);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, String output) {
        return 200;
    }
}

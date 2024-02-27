package no.sikt.nva.data.report.api.fetch;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import commons.db.GraphStoreProtocolConnection;
import java.util.List;
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

    private final QueryService queryService;

    @JacocoGenerated
    public FetchDataReport() {
        this(new QueryService(new GraphStoreProtocolConnection()));
    }

    public FetchDataReport(QueryService queryService) {
        super(Void.class);
        this.queryService = queryService;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return List.of(TEXT_CSV, TEXT_PLAIN, MICROSOFT_EXCEL);
    }

    @Override
    protected String processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        var reportRequest = ReportRequest.fromRequestInfo(requestInfo);
        var reportFormat = reportRequest.getReportFormat();
        var formatter = isCsvOrExcel(reportFormat) ? new CsvFormatter() : new PlainTextFormatter();
        return queryService.getResult(reportRequest, formatter);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, String output) {
        return 200;
    }

    private static boolean isCsvOrExcel(ReportFormat reportFormat) {
        return ReportFormat.CSV.equals(reportFormat) || ReportFormat.EXCEL.equals(reportFormat);
    }
}

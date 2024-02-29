package no.sikt.nva.data.report.api.fetch;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static com.google.common.net.MediaType.OOXML_SHEET;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import static no.sikt.nva.data.report.api.fetch.model.ReportFormat.EXCEL;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import commons.db.GraphStoreProtocolConnection;
import commons.formatter.ResponseFormatter;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.formatter.CsvFormatter;
import no.sikt.nva.data.report.api.fetch.formatter.ExcelFormatter;
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
        return List.of(TEXT_CSV, TEXT_PLAIN, MICROSOFT_EXCEL, OOXML_SHEET);
    }

    protected String processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        var reportRequest = ReportRequest.fromRequestInfo(requestInfo);
        var reportFormat = reportRequest.getReportFormat();
        var result = queryService.getResult(reportRequest, getFormatter(reportFormat));
        setIsBase64EncodedIfReportFormatExcel(reportFormat);
        return result;
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, String output) {
        return 200;
    }

    private static ResponseFormatter getFormatter(ReportFormat reportFormat) {
        return switch (reportFormat) {
            case CSV -> new CsvFormatter();
            case EXCEL -> new ExcelFormatter();
            case TEXT -> new PlainTextFormatter();
        };
    }

    private void setIsBase64EncodedIfReportFormatExcel(ReportFormat reportFormat) {
        if (EXCEL.equals(reportFormat)) {
            setIsBase64Encoded(true);
        }
    }
}

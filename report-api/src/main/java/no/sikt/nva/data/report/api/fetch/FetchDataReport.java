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
import java.util.Map;
import no.sikt.nva.data.report.api.fetch.formatter.CsvFormatter;
import no.sikt.nva.data.report.api.fetch.formatter.ExcelFormatter;
import no.sikt.nva.data.report.api.fetch.formatter.PlainTextFormatter;
import no.sikt.nva.data.report.api.fetch.model.ReportFormat;
import no.sikt.nva.data.report.api.fetch.model.ReportRequest;
import no.sikt.nva.data.report.api.fetch.service.DatabaseQueryService;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import no.sikt.nva.data.report.api.fetch.service.SparqlQueryGenerator;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.JacocoGenerated;

public class FetchDataReport extends ApiGatewayHandler<Void, String> {

    public static final String BEFORE_PLACEHOLDER = "__BEFORE__";
    public static final String AFTER_PLACEHOLDER = "__AFTER__";
    public static final String OFFSET_PLACEHOLDER = "__OFFSET__";
    public static final String PAGE_SIZE_PLACEHOLDER = "__PAGE_SIZE__";
    private final QueryService queryService;

    @JacocoGenerated
    public FetchDataReport() {
        this(new DatabaseQueryService(new GraphStoreProtocolConnection()));
    }

    public FetchDataReport(DatabaseQueryService queryService) {
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
        var sparqlQuery = SparqlQueryGenerator.getSparqlQuery(reportRequest.getReportType().getType(),
                                                              generateReplacementStrings(reportRequest));
        var result = queryService.getResult(sparqlQuery, getFormatter(reportFormat));
        setIsBase64EncodedIfReportFormatExcel(reportFormat);
        return result;
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, String output) {
        return 200;
    }

    private static Map<String, String> generateReplacementStrings(ReportRequest reportRequest) {
        return Map.of(
            BEFORE_PLACEHOLDER, reportRequest.getBefore().toString(),
            AFTER_PLACEHOLDER, reportRequest.getAfter().toString(),
            OFFSET_PLACEHOLDER, String.valueOf(reportRequest.getOffset()),
            PAGE_SIZE_PLACEHOLDER, String.valueOf(reportRequest.getPageSize())
        );
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

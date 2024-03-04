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
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import nva.commons.core.JacocoGenerated;

public class FetchNviInstitutionReport extends ApiGatewayHandler<Void, String> {

    public static final String ACCEPT = "Accept";
    public static final String NVI_INSTITUTION_STATUS_SPARQL = "nvi-institution-status";
    public static final String PATH_PARAMETER_REPORTING_YEAR = "reportingYear";
    private final QueryService queryService;

    @JacocoGenerated
    public FetchNviInstitutionReport() {
        this(new QueryService(new GraphStoreProtocolConnection()));
    }

    public FetchNviInstitutionReport(QueryService queryService) {
        super(Void.class);
        this.queryService = queryService;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return List.of(TEXT_CSV, TEXT_PLAIN, MICROSOFT_EXCEL, OOXML_SHEET);
    }

    @Override
    protected String processInput(Void unused, RequestInfo requestInfo, Context context) throws UnauthorizedException {
        validateAccessRights(requestInfo);
        var reportFormat = ReportFormat.fromMediaType(requestInfo.getHeader(ACCEPT));
        var reportingYear = requestInfo.getPathParameter(PATH_PARAMETER_REPORTING_YEAR);
        var result = queryService.getResult(NVI_INSTITUTION_STATUS_SPARQL, getFormatter(reportFormat));
        setIsBase64EncodedIfReportFormatExcel(reportFormat);
        return result;
    }

    @Override
    protected Integer getSuccessStatusCode(Void unused, String o) {
        return 200;
    }

    private static ResponseFormatter getFormatter(ReportFormat reportFormat) {
        return switch (reportFormat) {
            case CSV -> new CsvFormatter();
            case EXCEL -> new ExcelFormatter();
            case TEXT -> new PlainTextFormatter();
        };
    }

    private void validateAccessRights(RequestInfo requestInfo) throws UnauthorizedException {
        if (!requestInfo.userIsAuthorized(AccessRight.MANAGE_NVI)) {
            throw new UnauthorizedException();
        }
    }

    private void setIsBase64EncodedIfReportFormatExcel(ReportFormat reportFormat) {
        if (EXCEL.equals(reportFormat)) {
            setIsBase64Encoded(true);
        }
    }
}

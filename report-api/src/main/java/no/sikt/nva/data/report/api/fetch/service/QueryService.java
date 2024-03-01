package no.sikt.nva.data.report.api.fetch.service;

import commons.db.DatabaseConnection;
import commons.formatter.ResponseFormatter;
import java.nio.file.Path;
import no.sikt.nva.data.report.api.fetch.model.ReportRequest;
import no.sikt.nva.data.report.api.fetch.model.ReportType;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class QueryService {

    public static final String TEMPLATE_DIRECTORY = "template";
    public static final String SPARQL = ".sparql";
    public static final String BEFORE_PLACEHOLDER = "__BEFORE__";
    public static final String AFTER_PLACEHOLDER = "__AFTER__";
    public static final String OFFSET_PLACEHOLDER = "__OFFSET__";
    public static final String PAGE_SIZE_PLACEHOLDER = "__PAGE_SIZE__";
    private final DatabaseConnection databaseConnection;

    public QueryService(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public String getResult(ReportRequest reportRequest, ResponseFormatter formatter) {
        var query = getQuery(reportRequest);
        return databaseConnection.getResult(query, formatter);
    }

    public String getResult(ReportType reportType, ResponseFormatter formatter) {
        var query = getQuery(reportType);
        return databaseConnection.getResult(query, formatter);
    }

    private Query getQuery(ReportType reportType) {
        var template = Path.of(TEMPLATE_DIRECTORY, reportType.getType() + SPARQL);
        var sparqlTemplate = IoUtils.stringFromResources(template);
        return QueryFactory.create(sparqlTemplate);
    }

    private Query getQuery(ReportRequest reportRequest) {
        return QueryFactory.create(generateQuery(reportRequest));
    }

    private String generateQuery(ReportRequest reportRequest) {
        var template = Path.of(TEMPLATE_DIRECTORY, reportRequest.getReportType().getType() + SPARQL);
        var sparqlTemplate = IoUtils.stringFromResources(template);
        return sparqlTemplate.replace(BEFORE_PLACEHOLDER, reportRequest.getBefore().toString())
                   .replace(AFTER_PLACEHOLDER, reportRequest.getAfter().toString())
                   .replace(OFFSET_PLACEHOLDER, String.valueOf(reportRequest.getOffset()))
                   .replace(PAGE_SIZE_PLACEHOLDER, String.valueOf(reportRequest.getPageSize()));
    }
}

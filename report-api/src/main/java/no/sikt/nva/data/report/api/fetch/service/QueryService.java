package no.sikt.nva.data.report.api.fetch.service;

import java.nio.file.Path;
import no.sikt.nva.data.report.api.fetch.model.ReportRequest;
import no.sikt.nva.data.report.api.fetch.db.DatabaseConnection;
import no.sikt.nva.data.report.api.fetch.formatter.ResponseFormatter;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class QueryService {

    public static final String TEMPLATE_DIRECTORY = "template";
    public static final String SPARQL = ".sparql";
    private final DatabaseConnection databaseConnection;

    public QueryService(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public String getResult(ReportRequest reportRequest, ResponseFormatter formatter) {
        var query = getQuery(reportRequest);
        return databaseConnection.getResult(query, formatter);
    }

    // TODO: Actually use the reportRequest.
    private Query getQuery(ReportRequest reportRequest) {
        return QueryFactory.create(generateQuery(reportRequest));
    }

    private String generateQuery(ReportRequest reportRequest) {
        var template = Path.of(TEMPLATE_DIRECTORY, reportRequest.getReportType().getType() + SPARQL);
        var sparqlTemplate = IoUtils.stringFromResources(template);
        return sparqlTemplate.replace("__BEFORE__", reportRequest.getBefore().toString())
                   .replace("__AFTER__", reportRequest.getAfter().toString())
                   .replace("__OFFSET__", String.valueOf(reportRequest.getOffset()))
                   .replace("__PAGE_SIZE__", String.valueOf(reportRequest.getPageSize()));
    }
}

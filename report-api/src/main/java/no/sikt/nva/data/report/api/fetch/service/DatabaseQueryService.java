package no.sikt.nva.data.report.api.fetch.service;

import commons.db.DatabaseConnection;
import commons.formatter.ResponseFormatter;
import java.nio.file.Path;
import java.util.Map;
import no.sikt.nva.data.report.api.fetch.model.ReportRequest;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class DatabaseQueryService implements QueryService {

    public static final String TEMPLATE_DIRECTORY = "template";
    public static final String SPARQL = ".sparql";
    public static final String BEFORE_PLACEHOLDER = "__BEFORE__";
    public static final String AFTER_PLACEHOLDER = "__AFTER__";
    public static final String OFFSET_PLACEHOLDER = "__OFFSET__";
    public static final String PAGE_SIZE_PLACEHOLDER = "__PAGE_SIZE__";
    private final DatabaseConnection databaseConnection;

    public DatabaseQueryService(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public String getResult(ReportRequest reportRequest, ResponseFormatter formatter) {
        var query = getQuery(reportRequest);
        return databaseConnection.getResult(query, formatter);
    }

    @Override
    public String getResult(String sparqlTemplate, Map<String, String> replacementStrings,
                            ResponseFormatter formatter) {
        var query = getQuery(sparqlTemplate, replacementStrings);
        return databaseConnection.getResult(query, formatter);
    }

    private static Path constructPath(String sparqlTemplate) {
        return Path.of(TEMPLATE_DIRECTORY, sparqlTemplate + SPARQL);
    }

    private static String replaceStrings(String sparqlString, Map<String, String> replacementStrings) {
        var resultString = sparqlString;
        for (var entry : replacementStrings.entrySet()) {
            resultString = resultString.replace(entry.getKey(), entry.getValue());
        }
        return resultString;
    }

    private Query getQuery(String sparqlTemplate, Map<String, String> replacementStrings) {
        var template = constructPath(sparqlTemplate);
        var sparqlString = replaceStrings(IoUtils.stringFromResources(template), replacementStrings);
        return QueryFactory.create(sparqlString);
    }

    private Query getQuery(ReportRequest reportRequest) {
        return QueryFactory.create(generateQuery(reportRequest));
    }

    private String generateQuery(ReportRequest reportRequest) {
        var template = constructPath(reportRequest.getReportType().getType());
        var sparqlTemplate = IoUtils.stringFromResources(template);
        return sparqlTemplate.replace(BEFORE_PLACEHOLDER, reportRequest.getBefore().toString())
                   .replace(AFTER_PLACEHOLDER, reportRequest.getAfter().toString())
                   .replace(OFFSET_PLACEHOLDER, String.valueOf(reportRequest.getOffset()))
                   .replace(PAGE_SIZE_PLACEHOLDER, String.valueOf(reportRequest.getPageSize()));
    }
}

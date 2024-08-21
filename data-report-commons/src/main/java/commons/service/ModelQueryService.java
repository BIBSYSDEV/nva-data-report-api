package commons.service;

import commons.model.ReportType;
import java.nio.file.Path;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;

@JacocoGenerated //Used CsvTransformer and SingleObjectDataLoader, tested in modules bulk-export and data loading
public final class ModelQueryService {

    private static final String TEMPLATE_DIRECTORY = "template";
    private static final String SPARQL = ".sparql";

    public ModelQueryService() {
    }

    public ResultSetRewindable query(Model model, ReportType reportType) {
        var query = getQuery(reportType);
        try (var queryExecution = QueryExecutionFactory.create(query, model)) {
            var resultSet = queryExecution.execSelect();
            return ResultSetFactory.makeRewindable(resultSet);
        }
    }

    private static Path constructPath(String sparqlTemplate) {
        return Path.of(TEMPLATE_DIRECTORY, sparqlTemplate + SPARQL);
    }

    private static Query getQuery(ReportType reportType) {
        return QueryFactory.create(generateQuery(reportType));
    }

    private static String generateQuery(ReportType reportType) {
        var template = constructPath(reportType.getType());
        return IoUtils.stringFromResources(template);
    }
}

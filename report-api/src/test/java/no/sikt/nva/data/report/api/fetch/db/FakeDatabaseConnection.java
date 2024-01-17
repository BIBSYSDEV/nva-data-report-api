package no.sikt.nva.data.report.api.fetch.db;

import commons.db.DatabaseConnection;
import commons.formatter.ResponseFormatter;
import java.io.StringWriter;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class FakeDatabaseConnection implements DatabaseConnection {

    public static final Model model = ModelFactory.createDefaultModel();

    @Override
    public String getResult(Query query, ResponseFormatter formatter) {
        try (var queryExecution = QueryExecutionFactory.create(query, model)) {
            if (query.isSelectType()) {
                return formatter.format(queryExecution.execSelect());
            }
            throw new UnsupportedOperationException("The query method is unsupported, supported types: SELECT");
        }
    }

    @Override
    public void write(String triples, Lang lang) {

    }

    public void insert(Model data) {
        model.add(data);
    }

    // Helpful when debugging
    public String dump() {
        var stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, model, Lang.TURTLE);
        return stringWriter.toString();
    }

    public void flush() {
        model.removeAll();
    }
}

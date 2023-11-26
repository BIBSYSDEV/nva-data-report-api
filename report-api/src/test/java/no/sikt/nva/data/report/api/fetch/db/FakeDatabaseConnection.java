package no.sikt.nva.data.report.api.fetch.db;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import no.sikt.nva.data.report.api.fetch.formatter.ResponseFormatter;
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

    public void insert(String data) {
        RDFDataMgr.read(model, new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), Lang.NTRIPLES);
    }
}

package commons.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Test;

class CsvFormatterTest {

    @Test
    void shouldEscapeCommas() {
        var model = ModelFactory.createDefaultModel();
        var inputWithComma = "<http://example.org/subject> <http://example.org/predicate> \"value,with,commas\" .";
        RDFDataMgr.read(model, IoUtils.stringToStream(inputWithComma), Lang.NTRIPLES);
        var query = "SELECT * WHERE { ?s ?p ?o }";
        try (var queryExecution = QueryExecutionFactory.create(query, model)) {
            var resultSet = queryExecution.execSelect();
            var actual = new CsvFormatter().format(resultSet);
            var expected = """
                s,p,o
                http://example.org/subject,http://example.org/predicate,"value,with,commas"
                """;
            assertEquals(expected, actual);
        }
    }
}
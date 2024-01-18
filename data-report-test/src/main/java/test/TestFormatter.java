package test;

import commons.formatter.ResponseFormatter;
import org.apache.jena.query.ResultSet;

public final class TestFormatter implements ResponseFormatter {

    public TestFormatter() {
    }

    @Override
    public String format(ResultSet resultSet) {
        var triples = new StringBuilder();
        while (resultSet.hasNext()) {
            var current = resultSet.next();
            var vars = current.varNames();
            while (vars.hasNext()) {
                var x = current.get(vars.next());
                triples.append("<").append(x).append("> ");
            }
            triples.append(".");
        }
        return triples.isEmpty() ? null : triples.toString();
    }
}
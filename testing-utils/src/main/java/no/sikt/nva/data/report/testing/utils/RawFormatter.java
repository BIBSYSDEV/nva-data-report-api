package no.sikt.nva.data.report.testing.utils;

import commons.formatter.ResponseFormatter;
import java.util.stream.Collectors;
import org.apache.jena.query.ResultSet;

public final class RawFormatter implements ResponseFormatter {

    public RawFormatter() {
        // Simple object constructor.
    }

    @Override
    public String format(ResultSet resultSet) {
        var triples = new StringBuilder();
        while (resultSet.hasNext()) {
            var current = resultSet.next();
            var vars = current.varNames();
            while (vars.hasNext()) {
                var x = current.get(vars.next());
                triples.append(x);
                if (vars.hasNext()) {
                    triples.append(" ");
                }
            }
            triples.append(System.lineSeparator());
        }
        return triples.isEmpty()
                   ? null
                   : createSemiDeterministicOrderingOfTriples(triples);
    }

    private String createSemiDeterministicOrderingOfTriples(StringBuilder triples) {
        return triples.toString().lines().sorted(this::compare)
                   .collect(Collectors.joining(System.lineSeparator()));
    }

    private int compare(String s1, String s2) {
        return Integer.compare(s1.length(), s2.length());
    }
}
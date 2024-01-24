package no.sikt.nva.data.report.testing.utils;

import commons.formatter.ResponseFormatter;
import java.util.stream.Collectors;
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
                if (x.isLiteral()) {
                    triples.append('"')
                        .append(x.asLiteral().getValue())
                        .append('"')
                        .append('@')
                        .append(x.asLiteral().getLanguage())
                        .append(' ');
                } else {
                    triples.append("<").append(x).append("> ");
                }
            }
            triples.append(".").append(System.lineSeparator());
        }
        return triples.isEmpty() ? null : triples.toString().lines().sorted(this::compare).collect(Collectors.joining(System.lineSeparator()));
    }

    private int compare(String s1, String s2) {
        return Integer.compare(s1.length(), s2.length());
    }
}
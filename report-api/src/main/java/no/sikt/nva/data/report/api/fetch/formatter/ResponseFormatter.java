package no.sikt.nva.data.report.api.fetch.formatter;

import org.apache.jena.query.ResultSet;

public interface ResponseFormatter {
    String format(ResultSet resultSet);
}

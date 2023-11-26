package no.sikt.nva.data.report.api.fetch.formatter;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class PlainTextFormatter implements ResponseFormatter {

    @Override
    public String format(ResultSet resultSet) {
        return ResultSetFormatter.asText(resultSet);
    }
}

package no.sikt.nva.data.report.api.fetch.formatter;

import commons.formatter.ResponseFormatter;
import java.io.ByteArrayOutputStream;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class CsvFormatter implements ResponseFormatter {

    @Override
    public String format(ResultSet resultSet) {
        var outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsCSV(outputStream, resultSet);
        return outputStream.toString();
    }
}

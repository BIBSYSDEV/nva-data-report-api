package commons.formatter;

import java.io.ByteArrayOutputStream;
import nva.commons.core.JacocoGenerated;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

@JacocoGenerated //Tested in modules report-api and bulk-export
public class CsvFormatter implements ResponseFormatter {

    @Override
    public String format(ResultSet resultSet) {
        var outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsCSV(outputStream, resultSet);
        return outputStream.toString();
    }
}

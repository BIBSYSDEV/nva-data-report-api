package commons.formatter;

import org.apache.jena.query.ResultSet;

public interface ResponseFormatter {
    String format(ResultSet resultSet);
}

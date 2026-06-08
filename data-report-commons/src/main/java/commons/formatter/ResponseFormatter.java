package commons.formatter;

import org.apache.jena.query.ResultSet;

@FunctionalInterface
public interface ResponseFormatter {
  String format(ResultSet resultSet);
}

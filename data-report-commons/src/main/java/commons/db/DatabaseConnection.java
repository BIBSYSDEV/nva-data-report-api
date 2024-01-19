package commons.db;

import commons.formatter.ResponseFormatter;
import org.apache.jena.query.Query;
import org.apache.jena.riot.Lang;

public interface DatabaseConnection {

    void logConnection();

    String getResult(Query query, ResponseFormatter formatter);

    void write(String triples, Lang lang);

    void delete();
}

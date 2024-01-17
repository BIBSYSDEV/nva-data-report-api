package commons.db;

import commons.formatter.ResponseFormatter;
import org.apache.jena.query.Query;
import org.apache.jena.riot.Lang;

public interface DatabaseConnection {

    String getResult(Query query, ResponseFormatter formatter);

    void write(String triples, Lang lang);

}

package commons.db;

import commons.formatter.ResponseFormatter;
import java.net.URI;
import org.apache.jena.query.Query;
import org.apache.jena.riot.Lang;

public interface DatabaseConnection {

    String getResult(Query query, ResponseFormatter formatter);

    String fetch(URI graph);

    void write(URI graph, String triples, Lang lang);

    void delete(URI graph);
}

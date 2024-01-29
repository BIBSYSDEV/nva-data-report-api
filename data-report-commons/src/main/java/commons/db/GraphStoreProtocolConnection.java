package commons.db;

import commons.formatter.ResponseFormatter;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.system.Txn;

public class GraphStoreProtocolConnection implements DatabaseConnection {

    public static final String NONE = "none";
    public static final String EMPTY_STRING = "";
    public static final int HTTP_NOT_FOUND = 404;
    private static final String UNSUPPORTED_SPARQL_METHOD_MESSAGE = "The query method is unsupported, supported types:"
                                                                    + " SELECT";
    private static final String GSP_ENDPOINT = "gsp/";
    private static final String SPARQL_PATH = "sparql";
    private final String endpoint;
    private final String queryPath;

    @JacocoGenerated
    public GraphStoreProtocolConnection() {
        this(new Environment());
    }

    @JacocoGenerated
    private GraphStoreProtocolConnection(Environment environment) {
        this(constructEndPointUri(environment.readEnv("NEPTUNE_ENDPOINT"),
                                  environment.readEnv("NEPTUNE_PORT")),
             environment.readEnv("QUERY_PATH"));
    }

    public GraphStoreProtocolConnection(String endpoint, String queryPath) {
        this.endpoint = endpoint;
        this.queryPath = queryPath;
    }

    @Override
    public String getResult(Query query, ResponseFormatter formatter) {
        try (var connection = configureReadConnection()) {
            if (query.isSelectType()) {
                return formatter.format(transactionalQuery(query, connection));
            }
            throw new UnsupportedOperationException(UNSUPPORTED_SPARQL_METHOD_MESSAGE);
        }
    }

    @Override
    public String fetch(URI graph) {
        try (var connection = configureReadConnection()) {
            var data = connection.fetch(graph.toString());
            var stringWriter = new StringWriter();
            RDFDataMgr.write(stringWriter, data, Lang.NTRIPLES);
            return stringWriter.toString();
        } catch (Exception e) {
            throw filterExceptionToResend(e);
        }
    }

    @Override
    public void write(URI graph, String triples, Lang lang) {
        var inputStream = new ByteArrayInputStream(triples.getBytes(StandardCharsets.UTF_8));
        try (var connection = configureWriteConnection()) {
            var model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, inputStream, lang);
            connection.load(graph.toString(), model);
        } catch (Exception e) {
            throw filterExceptionToResend(e);
        }
    }

    @Override
    public void delete(URI graph) {
        try (var connection = configureWriteConnection()) {
            connection.delete(graph.toString());
        } catch (Exception e) {
            if (isNotFoundException(e)) {
                return;
            }
            throw new HttpException(e);
        }
    }

    private static ResultSet transactionalQuery(Query query, RDFConnection connection) {
        return Txn.calculateRead(connection, () -> {
            var resultSet = connection.query(query).execSelect();
            return ResultSetFactory.copyResults(resultSet);
        });
    }

    private static boolean isNotFoundException(Exception exception) {
        return exception instanceof HttpException httpException
               && httpException.getStatusCode() == HTTP_NOT_FOUND;
    }

    @JacocoGenerated
    private static String constructEndPointUri(String neptuneEndpoint, String neptunePort) {
        return String.format("https://%s:%s/%s", neptuneEndpoint, neptunePort, SPARQL_PATH);
    }

    private static RuntimeException filterExceptionToResend(Exception e) {
        if (e instanceof HttpException httpException) {
            return new HttpException(httpException.getStatusCode(), httpException.getStatusLine(),
                                     httpException.getResponse());
        } else {
            return new RuntimeException(e);
        }
    }

    private RDFConnection configureReadConnection() {
        var path = NONE.equalsIgnoreCase(queryPath) ? EMPTY_STRING : queryPath;
        return getRdfConnectionRemoteBuilder(endpoint)
                   .queryEndpoint(path)
                   .build();
    }

    private RDFConnection configureWriteConnection() {
        return getRdfConnectionRemoteBuilder(endpoint)
                   .build();
    }

    private RDFConnectionRemoteBuilder getRdfConnectionRemoteBuilder(String endpoint) {
        return RDFConnectionRemote.newBuilder()
                   .destination(endpoint)
                   .gspEndpoint(GSP_ENDPOINT)
                   .httpClient(HttpClient.newHttpClient());
    }
}

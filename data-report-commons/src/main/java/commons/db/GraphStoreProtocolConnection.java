package commons.db;

import static java.util.Objects.nonNull;
import commons.formatter.ResponseFormatter;
import java.io.ByteArrayInputStream;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphStoreProtocolConnection implements DatabaseConnection {

    public static final String UNSUPPORTED_SPARQL_METHOD_MESSAGE = "The query method is unsupported, supported types:"
                                                                   + " SELECT";
    public static final String QSP_ENDPOINT = "gsp";
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphStoreProtocolConnection.class);
    private final HttpClient httpClient;
    private String endpoint;
    private String port;

    @JacocoGenerated
    public GraphStoreProtocolConnection() {
        this(new Environment());
    }

    @JacocoGenerated
    private GraphStoreProtocolConnection(Environment environment) {
        this(environment.readEnv("NEPTUNE_ENDPOINT"),
             environment.readEnv("NEPTUNE_PORT"));
    }

    public GraphStoreProtocolConnection(String destination, String gspEndpoint) {
        this.httpClient = useDefault();
        this.endpoint = destination;
        this.port = gspEndpoint;
    }

    public void logConnection() {
        try (var connection = configureRemoteConnection()) {
            var model = connection.fetch();
            LOGGER.info("Connection to {} successful, model size: {}", endpoint, model.size());
        }
    }

    @Override
    public String getResult(Query query, ResponseFormatter formatter) {
        try (var queryExecution = configureQueryExecution(query)) {
            if (query.isSelectType()) {
                return formatter.format(queryExecution.execSelect());
            }
            throw new UnsupportedOperationException(UNSUPPORTED_SPARQL_METHOD_MESSAGE);
        }
    }

    @Override
    public void write(String triples, Lang lang) {
        var inputStream = new ByteArrayInputStream(triples.getBytes(StandardCharsets.UTF_8));
        try (var connection = configureRemoteConnection()) {
            var model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, inputStream, lang);
            connection.load(model);
        }
    }

    private RDFConnection configureRemoteConnection() {
        return RDFConnectionRemote.newBuilder()
                   .gspEndpoint(QSP_ENDPOINT)
                   .destination(endpoint)
                   .httpClient(HttpClient.newHttpClient())
                   .build();
    }

    @JacocoGenerated
    private HttpClient useDefault() {
        return HttpClient.newHttpClient();
    }

    private QueryExecution configureQueryExecution(Query query) {
        var builder = QueryExecution.service(endpoint);
        if (nonNull(httpClient)) {
            builder.httpClient(httpClient);
        }
        return builder.query(query).build();
    }

    private String getEndpoint() {
        return String.format("https://%s:%s/sparql", endpoint, port);
    }
}

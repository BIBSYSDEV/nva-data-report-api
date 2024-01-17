package commons.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import commons.formatter.ResponseFormatter;
import nva.commons.core.Environment;
import nva.commons.logutils.LogUtils;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphStoreProtocolConnectionTest {

    private static final String GSP_ENDPOINT = "/gsp";
    private FusekiServer server;
    private GraphStoreProtocolConnection dbConnection;

    @BeforeEach
    void setUp() {
        var dataSet = DatasetFactory.createTxnMem();
        initializeGraphServer(dataSet);
        var url = server.serverURL();
        dbConnection = new GraphStoreProtocolConnection(url, url, new Environment().readEnv("QUERY_PATH"));
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldWrite() {
        var triple = "<https://example.org/a> <https://example.org/b> <https://example.org/c> .";
        dbConnection.write(triple, Lang.NTRIPLES);
        var query = QueryFactory.create("SELECT * WHERE { ?a ?b ?c }");
        var result = dbConnection.getResult(query, new TestFormatter());
        assertEquals(triple, result);
    }

    @Test
    void shouldLogConnection() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        dbConnection.logConnection();
        assertTrue(logAppender.getMessages().contains("Connection"));
    }

    @Test
    void shouldDelete() {
        var triple = "<https://example.org/a> <https://example.org/b> <https://example.org/c> .";
        dbConnection.write(triple, Lang.NTRIPLES);
        dbConnection.delete();
        var query = QueryFactory.create("SELECT * WHERE { ?a ?b ?c }");
        var result = dbConnection.getResult(query, new TestFormatter());
        assertNull(result);
    }

    private void initializeGraphServer(Dataset dataSet) {
        server = FusekiServer.create()
                     .add(GSP_ENDPOINT, dataSet)
                     .build();
        server.start();
    }

    private static class TestFormatter implements ResponseFormatter {

        @Override
        public String format(ResultSet resultSet) {
            var triples = new StringBuilder();
            while (resultSet.hasNext()) {
                var current = resultSet.next();
                var vars = current.varNames();
                while (vars.hasNext()) {
                    var x = current.get(vars.next());
                    triples.append("<").append(x).append("> ");
                }
                triples.append(".");
            }
            return triples.isEmpty() ? null : triples.toString();
        }
    }
}
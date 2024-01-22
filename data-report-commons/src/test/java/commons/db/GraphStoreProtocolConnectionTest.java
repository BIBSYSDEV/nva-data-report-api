package commons.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import commons.formatter.ResponseFormatter;
import java.net.URI;
import java.util.Random;
import no.sikt.nva.data.report.testing.utils.FusekiTestingServer;
import nva.commons.core.Environment;
import nva.commons.logutils.LogUtils;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GraphStoreProtocolConnectionTest {

    private static final String GSP_ENDPOINT = "/gsp";
    private static final URI GRAPH = URI.create("https://example.org/graph");
    private static FusekiServer server;
    private static GraphStoreProtocolConnection dbConnection;

    /**
     * Tests re-use the same database. This is intentional as it is the case
     * for the production environment;
     */
    @BeforeAll
    static void setUp() {
        var dataSet = DatasetFactory.createTxnMem();
        server = FusekiTestingServer.init(dataSet, GSP_ENDPOINT);
        var url = server.serverURL();
        dbConnection = new GraphStoreProtocolConnection(url, url, new Environment().readEnv("QUERY_PATH"));
    }

    @AfterAll
    static void tearDown() {
        server.stop();
    }

    @AfterEach
    void clearDatabase() {
        try {
            dbConnection.delete(GRAPH);
        } catch (Exception e) {
            // Necessary to avoid case where we hve already deleted the graph
            catchExpectedExceptionsExceptHttpException(e);
        }
    }

    @Test
    void shouldWrite() {
        var random = new Random().nextInt();
        var triple = "<https://example.org/a> <https://example.org/b> <https://example.org/c" + random + "> .";
        dbConnection.write(GRAPH, triple, Lang.NTRIPLES);
        var query = QueryFactory.create("SELECT ?a ?b ?c WHERE { GRAPH ?g { ?a ?b ?c } }");
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
        dbConnection.write(GRAPH, triple, Lang.NTRIPLES);
        assertNotNull(dbConnection.fetch(GRAPH));
        dbConnection.delete(GRAPH);
        assertThatTheGraphDoesNotExist(GRAPH);
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

    private static void assertThatTheGraphDoesNotExist(URI graph) {
        try {
            dbConnection.fetch(graph);
        } catch (Exception exception) {
            assertInstanceOf(HttpException.class, exception);
            assertEquals(404, ((HttpException) exception).getStatusCode());
        }
    }

    private static void catchExpectedExceptionsExceptHttpException(Exception e) {
        if (!(e instanceof HttpException)) {
            throw new RuntimeException(e);
        }
    }
}
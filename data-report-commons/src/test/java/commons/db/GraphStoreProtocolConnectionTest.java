package commons.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import no.sikt.nva.data.report.testing.utils.FusekiTestingServer;
import nva.commons.core.Environment;
import nva.commons.logutils.LogUtils;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import no.sikt.nva.data.report.testing.utils.TestFormatter;

class GraphStoreProtocolConnectionTest {

    private static final String GSP_ENDPOINT = "/gsp";
    private static FusekiServer server;
    private static GraphStoreProtocolConnection dbConnection;

    /**
     * Tests re-use the same database. This is intentional as it is the case for the production environment;
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
        dbConnection.delete();
    }

    @Test
    void shouldWrite() {
        var random = new Random().nextInt();
        var triple = "<https://example.org/a> <https://example.org/b> <https://example.org/c" + random + "> .";
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
}
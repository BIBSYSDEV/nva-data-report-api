package commons.db;

import static org.junit.jupiter.api.Assertions.*;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
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
        server = FusekiServer.create()
                     .add(GSP_ENDPOINT, dataSet)
                     .build();
        dbConnection = new GraphStoreProtocolConnection(server.serverURL(), GSP_ENDPOINT);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldWrite() {
        dbConnection.write("<https://example.org/a> <https://example.org/b> <https://example.org/c> .", Lang.NTRIPLES);
    }
}
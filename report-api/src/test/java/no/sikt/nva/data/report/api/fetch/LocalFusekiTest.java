package no.sikt.nva.data.report.api.fetch;

import commons.db.DatabaseConnection;
import commons.db.GraphStoreProtocolConnection;
import java.io.StringWriter;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import no.sikt.nva.data.report.api.fetch.testutils.generator.TestData.DatePair;
import no.sikt.nva.data.report.api.fetch.testutils.generator.publication.PublicationDate;
import no.sikt.nva.data.report.testing.utils.FusekiTestingServer;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

public abstract class LocalFusekiTest {

    static final String GSP_ENDPOINT = "/gsp";
    static final String GRAPH_BASE_URI = "https://example.org/graph/";
    static List<URI> graphsRegisteredForDeletion = new ArrayList<>();
    static FusekiServer server;
    static DatabaseConnection databaseConnection;

    @BeforeAll
    static void setup() {
        var dataSet = DatasetFactory.createTxnMem();
        server = FusekiTestingServer.init(dataSet, GSP_ENDPOINT);
        var url = server.serverURL();
        var queryPath = new Environment().readEnv("QUERY_PATH");
        databaseConnection = new GraphStoreProtocolConnection(url, url, queryPath);
    }

    @AfterAll
    static void tearDown() {
        server.stop();
    }

    @AfterEach
    void clearDatabase() {
        try {
            graphsRegisteredForDeletion.forEach(graph -> databaseConnection.delete(graph));
            graphsRegisteredForDeletion = new ArrayList<>();
        } catch (Exception e) {
            // Necessary to avoid case where we hve already deleted the graph
            catchExpectedExceptionsExceptHttpException(e);
        }
    }

    void loadModels(List<Model> models) {
        models.stream()
            .map(this::toTriples)
            .forEach(triples -> {
                var graph = UriWrapper.fromHost(GRAPH_BASE_URI).addChild(UUID.randomUUID().toString()).getUri();
                graphsRegisteredForDeletion.add(graph);
                databaseConnection.write(graph, triples, Lang.NTRIPLES);
            });
    }

    String toTriples(Model model) {
        var stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, model, Lang.NTRIPLES);
        return stringWriter.toString();
    }

    List<DatePair> generateDatePairs(int numberOfDatePairs) {
        return IntStream.range(0, numberOfDatePairs)
                   .mapToObj(i -> new DatePair(new PublicationDate("2024", "02", "02"),
                                               Instant.now().minus(100, ChronoUnit.DAYS)))
                   .toList();
    }

    private static void catchExpectedExceptionsExceptHttpException(Exception e) {
        if (!(e instanceof HttpException)) {
            throw new RuntimeException(e);
        }
    }
}

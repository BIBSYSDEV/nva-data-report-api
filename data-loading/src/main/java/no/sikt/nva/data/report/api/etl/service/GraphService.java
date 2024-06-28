package no.sikt.nva.data.report.api.etl.service;

import commons.db.DatabaseConnection;
import java.net.URI;
import no.sikt.nva.data.report.api.etl.NTriples;
import org.apache.jena.riot.Lang;

public class GraphService {

    private final DatabaseConnection databaseConnection;

    public GraphService(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public void persist(URI graph, String resource) {
        var triples = NTriples.transform(resource).toString();
        databaseConnection.delete(graph);
        databaseConnection.write(graph, triples, Lang.NTRIPLES);
    }
}

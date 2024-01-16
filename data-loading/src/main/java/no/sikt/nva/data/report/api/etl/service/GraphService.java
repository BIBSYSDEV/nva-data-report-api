package no.sikt.nva.data.report.api.etl.service;

import commons.db.GraphStoreProtocolConnection;

public class GraphService {

    private final GraphStoreProtocolConnection databaseConnection;

    public GraphService(GraphStoreProtocolConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
        databaseConnection.logConnection();
    }
}

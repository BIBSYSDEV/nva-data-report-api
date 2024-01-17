package no.sikt.nva.data.report.api.etl.service;

import commons.db.GraphStoreProtocolConnection;

public class GraphService {

    public GraphService(GraphStoreProtocolConnection databaseConnection) {
        databaseConnection.logConnection();
    }
}

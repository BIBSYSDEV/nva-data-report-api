package no.sikt.nva.data.report.api.etl.service;

import commons.db.DatabaseConnection;

public class GraphService {

    public GraphService(DatabaseConnection databaseConnection) {
        databaseConnection.logConnection();
    }
}

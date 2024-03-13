package no.sikt.nva.data.report.api.fetch.service;

import commons.db.DatabaseConnection;
import commons.formatter.ResponseFormatter;
import org.apache.jena.query.QueryFactory;

public class DatabaseQueryService implements QueryService {

    private final DatabaseConnection databaseConnection;

    public DatabaseQueryService(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public String getResult(String sparqlQuery, ResponseFormatter formatter) {
        var query = QueryFactory.create(sparqlQuery);
        return databaseConnection.getResult(query, formatter);
    }
}

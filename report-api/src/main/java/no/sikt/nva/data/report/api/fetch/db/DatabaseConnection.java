package no.sikt.nva.data.report.api.fetch.db;

import no.sikt.nva.data.report.api.fetch.formatter.ResponseFormatter;
import org.apache.jena.query.Query;

public interface DatabaseConnection {

    String getResult(Query query, ResponseFormatter formatter);
}

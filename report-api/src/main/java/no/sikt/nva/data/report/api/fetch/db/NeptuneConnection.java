package no.sikt.nva.data.report.api.fetch.db;

import static java.util.Objects.nonNull;
import java.net.http.HttpClient;
import no.sikt.nva.data.report.api.fetch.formatter.ResponseFormatter;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class NeptuneConnection implements DatabaseConnection {

    public static final Environment ENVIRONMENT = new Environment();

    private final HttpClient httpClient;

    @JacocoGenerated
    public NeptuneConnection() {
        this.httpClient = useDefault();
    }

    public NeptuneConnection(HttpClient client) {
        this.httpClient = client;
    }

    @Override
    public String getResult(Query query, ResponseFormatter formatter) {
        try (var queryExecution = configureQueryExecution(query)) {
            if (query.isSelectType()) {
                return formatter.format(queryExecution.execSelect());
            }
            throw new UnsupportedOperationException("The query method is unsupported, supported types: SELECT");
        }
    }

    private HttpClient useDefault() {
        return null;
    }

    private QueryExecution configureQueryExecution(Query query) {
        var builder = QueryExecution.service(getEndpoint());
        if (nonNull(httpClient)) {
            builder.httpClient(httpClient);
        }

        return builder.query(query)
                   .build();
    }

    private static String getEndpoint() {
        var endpoint = ENVIRONMENT.readEnv("NEPTUNE_ENDPOINT");
        var port = ENVIRONMENT.readEnv("NEPTUNE_PORT");
        return String.format("https://%s:%s/sparql", endpoint, port);
    }
}

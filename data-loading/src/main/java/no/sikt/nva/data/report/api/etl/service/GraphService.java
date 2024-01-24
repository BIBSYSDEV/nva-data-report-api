package no.sikt.nva.data.report.api.etl.service;

import static java.util.Objects.isNull;
import commons.db.DatabaseConnection;
import java.io.InputStream;
import java.net.URI;
import no.sikt.nva.data.report.api.etl.utils.RdfUtil;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;

public class GraphService {

    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GraphService.class);

    private final DatabaseConnection databaseConnection;

    public GraphService(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
        this.databaseConnection.logConnection();
    }

    public void persist(URI graph, String resource) {
        var nTriples = toNTriples(resource);
        LOGGER.info("Writing nTriples to graph: {}", nTriples);
        databaseConnection.write(graph, nTriples, Lang.NTRIPLES);
        LOGGER.info("Successfully wrote nTriples to graph: {}", graph);
        LOGGER.info("Fetching graph: {}", graph);
        var result = databaseConnection.fetch(graph);
        LOGGER.info("Successfully fetched graph: {}. Result {}", graph, result);
    }

    private static String toNTriples(String resource) {
        var model = ModelFactory.createDefaultModel();
        loadDataIntoModel(model, IoUtils.stringToStream(resource));
        return RdfUtil.toNTriples(model);
    }

    @JacocoGenerated
    private static void loadDataIntoModel(Model model, InputStream inputStream) {
        if (isNull(inputStream)) {
            return;
        }
        try {
            RDFDataMgr.read(model, inputStream, Lang.JSONLD);
        } catch (RiotException e) {
            logInvalidJsonLdInput(e);
        }
    }

    @JacocoGenerated
    private static void logInvalidJsonLdInput(Exception exception) {
        LOGGER.error("Invalid JSON LD input encountered: ", exception);
    }
}

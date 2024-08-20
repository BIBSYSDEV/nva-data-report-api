package no.sikt.nva.data.report.api.etl;

import static no.sikt.nva.data.report.api.etl.model.EventType.UPSERT;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import commons.StorageReader;
import commons.ViewCompiler;
import commons.db.GraphStoreProtocolConnection;
import commons.db.utils.DocumentUnwrapper;
import java.net.URI;
import no.sikt.nva.data.report.api.etl.model.EventType;
import no.sikt.nva.data.report.api.etl.model.PersistedResourceEvent;
import no.sikt.nva.data.report.api.etl.service.GraphService;
import no.sikt.nva.data.report.api.etl.service.S3StorageReader;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleObjectDataLoader implements RequestHandler<PersistedResourceEvent, Void> {

    public static final Logger LOGGER = LoggerFactory.getLogger(SingleObjectDataLoader.class);
    public static final String EXPANDED_RESOURCES_BUCKET = "EXPANDED_RESOURCES_BUCKET";
    public static final String API_HOST = "API_HOST";
    private final StorageReader<UnixPath> storageReader;
    GraphService graphService;

    @JacocoGenerated
    public SingleObjectDataLoader() {
        this(new GraphService(new GraphStoreProtocolConnection()),
             new S3StorageReader(new Environment().readEnv(EXPANDED_RESOURCES_BUCKET)));
    }

    public SingleObjectDataLoader(GraphService graphService, StorageReader<UnixPath> storageReader) {
        LOGGER.info("Initializing SingleObjectDataLoader");
        this.graphService = graphService;
        this.storageReader = storageReader;
    }

    @Override
    public Void handleRequest(PersistedResourceEvent input, Context context) {
        input.validate();
        logInput(input);
        var eventType = EventType.parse(input.eventType());
        if (UPSERT.equals(eventType)) {
            storeObject(UnixPath.of(input.key()));
        }
        return null;
    }

    private static JsonNode toJsonNode(String blob) {
        var documentUnwrapper = new DocumentUnwrapper(new Environment().readEnv(API_HOST));
        return attempt(() -> documentUnwrapper.unwrap(blob)).orElseThrow();
    }

    private void storeObject(UnixPath objectKey) {
        var blob = storageReader.read(objectKey);
        var resource = toJsonNode(blob);
        var id = URI.create(resource.at("/id").textValue());
        var graph = URI.create(id + ".nt");
        graphService.persist(graph, applyView(resource, id));
        LOGGER.info("Persisted object with key: {} in graph: {}", objectKey, graph);
    }

    private Model applyView(JsonNode resource, URI id) {
        return new ViewCompiler(IoUtils.stringToStream(resource.toString())).extractView(id);
    }

    private void logInput(PersistedResourceEvent input) {
        LOGGER.info("Input object key: {}, eventType: {}", input.key(), input.eventType());
    }
}

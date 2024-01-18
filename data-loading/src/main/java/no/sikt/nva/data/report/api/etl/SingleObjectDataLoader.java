package no.sikt.nva.data.report.api.etl;

import static no.sikt.nva.data.report.api.etl.utils.DocumentUnwrapper.unwrap;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import commons.StorageReader;
import commons.db.GraphStoreProtocolConnection;
import no.sikt.nva.data.report.api.etl.model.PersistedResourceEvent;
import no.sikt.nva.data.report.api.etl.service.GraphService;
import no.sikt.nva.data.report.api.etl.service.S3StorageReader;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleObjectDataLoader implements RequestHandler<PersistedResourceEvent, Void> {

    public static final Logger LOGGER = LoggerFactory.getLogger(SingleObjectDataLoader.class);
    public static final String EXPANDED_RESOURCES_BUCKET = "EXPANDED_RESOURCES_BUCKET";
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

    //TODO: Handle different event types
    //TODO: Handle failures
    @Override
    public Void handleRequest(PersistedResourceEvent input, Context context) {
        input.validate();
        logInput(input);
        storeObject(UnixPath.of(input.key()));
        return null;
    }

    private void storeObject(UnixPath objectKey) {
        var blob = storageReader.read(objectKey);
        var resource = attempt(() -> unwrap(blob)).orElseThrow();
        graphService.convertAndStore(resource);
    }

    private void logInput(PersistedResourceEvent input) {
        LOGGER.info("Input object key: {}, eventType: {}", input.key(), input.eventType());
    }
}

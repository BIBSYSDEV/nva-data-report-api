package no.sikt.nva.data.report.api.etl;

import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import commons.db.GraphStoreProtocolConnection;
import java.util.Optional;
import no.sikt.nva.data.report.api.etl.model.EventType;
import no.sikt.nva.data.report.api.etl.model.PersistedResourceEvent;
import no.sikt.nva.data.report.api.etl.service.GraphService;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleObjectDataLoader implements RequestHandler<PersistedResourceEvent, Void> {

    public static final Logger LOGGER = LoggerFactory.getLogger(SingleObjectDataLoader.class);

    GraphService graphService;

    @JacocoGenerated
    public SingleObjectDataLoader() {
        this(new GraphService(new GraphStoreProtocolConnection()));
    }

    public SingleObjectDataLoader(GraphService graphService) {
        LOGGER.info("Initializing DataLoader");
        this.graphService = graphService;
    }

    @Override
    public Void handleRequest(PersistedResourceEvent input, Context context) {
        LOGGER.info("Handling request with input: {}", input.toString());
        logParentFolder(input);
        logEventType(input);
        return null;
    }

    private void logParentFolder(PersistedResourceEvent input) {
        UnixPath.of(input.key()).getParent().ifPresentOrElse(this::logFolderName, this::logNoParentFolder);
    }

    private void logEventType(PersistedResourceEvent input) {
        getEventType(input).ifPresentOrElse(this::logEventType, () -> logUnknownEventType(input.eventType()));
    }

    private void logEventType(EventType eventType) {
        LOGGER.info("Event type: {}", eventType);
    }

    private void logFolderName(UnixPath folder) {
        LOGGER.info("Object folder: {}", folder);
    }

    private Optional<EventType> getEventType(PersistedResourceEvent input) {
        return attempt(() -> EventType.parse(input.eventType())).toOptional();
    }

    private void logUnknownEventType(String eventType) {
        LOGGER.error("Unknown event type: {}", eventType);
    }

    private void logNoParentFolder() {
        LOGGER.info("No parent folder");
    }
}

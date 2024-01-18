package no.sikt.nva.data.report.api.etl;

import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import commons.db.GraphStoreProtocolConnection;
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
        LOGGER.info("Initializing SingleObjectDataLoader");
        this.graphService = graphService;
    }

    @Override
    public Void handleRequest(PersistedResourceEvent input, Context context) {
        logInput(input);
        return null;
    }

    private static String extractParentFolder(PersistedResourceEvent input) {
        return UnixPath.of(input.key())
                   .getParent()
                   .map(UnixPath::toString)
                   .orElse("No parent folder");
    }

    private static String extractEventType(PersistedResourceEvent input) {
        return attempt(() -> EventType.parse(input.eventType()))
                   .map(EventType::getValue)
                   .orElse(failure -> "Unknown event type: " + input.eventType());
    }

    private void logInput(PersistedResourceEvent input) {
        LOGGER.info("Input key: {}, object folder: {}, eventType: {}", input.key(), extractParentFolder(input),
                    extractEventType(input));
    }
}

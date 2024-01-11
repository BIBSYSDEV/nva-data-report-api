package no.sikt.nva.data.report.api.etl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import nva.commons.core.paths.UnixPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleObjectDataLoader implements RequestHandler<PersistedResourceEvent, Void> {

    public static final Logger LOGGER = LoggerFactory.getLogger(SingleObjectDataLoader.class);

    public SingleObjectDataLoader() {
        LOGGER.info("Initializing DataLoader");
    }

    @Override
    public Void handleRequest(PersistedResourceEvent input, Context context) {
        LOGGER.info("Handling request with input: {}", input.toString());
        UnixPath.of(input.key()).getParent().ifPresentOrElse(this::logFolderName, this::logNoParentFolder);
        return null;
    }

    private void logNoParentFolder() {
        LOGGER.info("No parent folder");
    }

    private void logFolderName(UnixPath folder) {
        LOGGER.info("Object folder: {}", folder);
    }
}

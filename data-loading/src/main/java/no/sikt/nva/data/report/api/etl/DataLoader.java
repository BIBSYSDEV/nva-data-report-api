package no.sikt.nva.data.report.api.etl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataLoader implements RequestHandler<SQSEvent, Void> {
    public static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    public DataLoader() {
        LOGGER.info("Initializing DataLoader");
    }

    @Override
    public Void handleRequest(SQSEvent input, Context context) {
        return null;
    }
}

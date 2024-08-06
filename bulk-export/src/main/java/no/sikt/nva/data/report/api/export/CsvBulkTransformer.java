package no.sikt.nva.data.report.api.export;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import nva.commons.core.JacocoGenerated;

public class CsvBulkTransformer extends EventHandler<KeyBatchEvent, Void> {

    @JacocoGenerated
    public CsvBulkTransformer() {
        super(KeyBatchEvent.class);
    }

    @Override
    protected Void processInput(KeyBatchEvent keyBatchEvent, AwsEventBridgeEvent<KeyBatchEvent> event,
                                Context context) {
        return null;
    }
}

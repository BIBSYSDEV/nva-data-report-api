package no.sikt.nva.data.report.api.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;

public class BulkReportGenerator extends EventHandler<GenerateReportRequest, Void> {

    protected BulkReportGenerator() {
        super(GenerateReportRequest.class);
    }

    @Override
    protected Void processInput(GenerateReportRequest generateReportRequest,
                                AwsEventBridgeEvent<GenerateReportRequest> awsEventBridgeEvent, Context context) {
        return null;
    }
}

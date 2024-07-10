package no.sikt.nva.data.report.api.fetch;

import static java.util.Objects.isNull;
import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;

public class BulkReportGenerator extends EventHandler<GenerateReportRequest, Void> {

    protected BulkReportGenerator() {
        super(GenerateReportRequest.class);
    }

    @Override
    protected Void processInput(GenerateReportRequest request, AwsEventBridgeEvent<GenerateReportRequest> event,
                                Context context) {
        validate(request);
        return null;
    }

    private static void validate(GenerateReportRequest request) {
        if (isNull(request.reportType())) {
            throw new IllegalArgumentException("Report type is required");
        }
    }
}

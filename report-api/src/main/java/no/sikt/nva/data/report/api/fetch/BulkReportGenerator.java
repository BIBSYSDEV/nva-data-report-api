package no.sikt.nva.data.report.api.fetch;

import static java.util.Objects.isNull;
import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.data.report.api.fetch.model.ReportType;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;

public class BulkReportGenerator extends EventHandler<BulkReportRequest, Void> {

    protected BulkReportGenerator() {
        super(BulkReportRequest.class);
    }

    @Override
    protected Void processInput(BulkReportRequest request, AwsEventBridgeEvent<BulkReportRequest> event,
                                Context context) {
        validate(request);
        ReportType.parse(request.reportType());
        return null;
    }

    private static void validate(BulkReportRequest request) {
        if (isNull(request.reportType())) {
            throw new IllegalArgumentException("Report type is required");
        }
    }
}

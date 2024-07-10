package no.sikt.nva.data.report.api.fetch;

import static java.util.Objects.isNull;
import com.amazonaws.services.lambda.runtime.Context;
import no.sikt.nva.data.report.api.fetch.model.ReportType;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BulkReportGenerator extends EventHandler<BulkReportRequest, Void> {

    private static final Logger logger = LoggerFactory.getLogger(BulkReportGenerator.class);

    protected BulkReportGenerator() {
        super(BulkReportRequest.class);
    }

    @Override
    protected Void processInput(BulkReportRequest request, AwsEventBridgeEvent<BulkReportRequest> event,
                                Context context) {
        validate(request);
        var reportType = ReportType.parse(request.reportType());
        logger.info("Report type: {}", reportType.getType());
        return null;
    }

    private static void validate(BulkReportRequest request) {
        if (isNull(request.reportType())) {
            throw new IllegalArgumentException("Report type is required");
        }
    }
}

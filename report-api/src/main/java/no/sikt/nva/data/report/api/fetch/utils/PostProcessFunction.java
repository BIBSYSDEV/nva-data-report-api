package no.sikt.nva.data.report.api.fetch.utils;

import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.util.function.Function;
import no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders;

public enum PostProcessFunction {
    GLOBAL_STATUS(NviInstitutionStatusHeaders.GLOBAL_STATUS, PostProcessFunction::postProcessGlobalApprovalStatus),
    INTERNATIONAL_COLLABORATION_FACTOR(NviInstitutionStatusHeaders.INTERNATIONAL_COLLABORATION_FACTOR,
                                       PostProcessFunction::processDecimalValue),
    PUBLICATION_CHANNEL_LEVEL_POINTS(NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_LEVEL_POINTS,
                                     PostProcessFunction::processDecimalValue)
    ;

    private final String header;
    private final Function<String, String> postProcessor;

    PostProcessFunction(String header, Function<String, String> postProcessor) {
        this.header = header;
        this.postProcessor = postProcessor;
    }

    public String getHeader() {
        return header;
    }

    public Function<String, String> getPostProcessor() {
        return postProcessor;
    }

    private static String processDecimalValue(String decimalValue) {
        var parts = decimalValue.split("\\^\\^");
        return parts[0].replace("\"", EMPTY_STRING);
    }

    private static String postProcessGlobalApprovalStatus(String rawValue) {
        return switch (rawValue) {
            case "Pending" -> "?";
            case "Approved" -> "J";
            case "Dispute" -> "T";
            case "Rejected" -> "N";
            default -> rawValue;
        };
    }
}

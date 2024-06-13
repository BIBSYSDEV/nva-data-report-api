package no.sikt.nva.data.report.api.fetch.utils;

import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.util.function.Function;
import no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders;
import nva.commons.core.paths.UriWrapper;

public enum PostProcessFunction {
    GLOBAL_STATUS(NviInstitutionStatusHeaders.GLOBAL_STATUS, PostProcessFunction::postProcessGlobalApprovalStatus),
    INTERNATIONAL_COLLABORATION_FACTOR(NviInstitutionStatusHeaders.INTERNATIONAL_COLLABORATION_FACTOR,
                                       PostProcessFunction::getDecimalValue),
    PUBLICATION_CHANNEL_LEVEL_POINTS(NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_LEVEL_POINTS,
                                     PostProcessFunction::getDecimalValue),
    PUBLICATION_IDENTIFIER(NviInstitutionStatusHeaders.PUBLICATION_IDENTIFIER,
                           PostProcessFunction::getIdentifierFromUri),
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

    private static String getIdentifierFromUri(String uri) {
        return UriWrapper.fromUri(uri).getLastPathElement();
    }

    private static String getDecimalValue(String decimalValue) {
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

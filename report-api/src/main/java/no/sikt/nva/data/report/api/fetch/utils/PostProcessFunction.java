package no.sikt.nva.data.report.api.fetch.utils;

import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.DEPARTMENT_ID;
import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.FACULTY_ID;
import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.GLOBAL_STATUS;
import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.GROUP_ID;
import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.INSTITUTION_APPROVAL_STATUS;
import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.INSTITUTION_ID;
import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.INTERNATIONAL_COLLABORATION_FACTOR;
import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_LEVEL;
import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_LEVEL_POINTS;
import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_TYPE;
import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.PUBLICATION_IDENTIFIER;
import static no.sikt.nva.data.report.api.fetch.model.NviInstitutionStatusHeaders.PUBLICATION_INSTANCE;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.util.function.Function;
import nva.commons.core.paths.UriWrapper;

public enum PostProcessFunction {
    GLOBAL_STATUS_FUNCTION(GLOBAL_STATUS, PostProcessFunction::postProcessGlobalApprovalStatus),
    INTERNATIONAL_COLLABORATION_FACTOR_FUNCTION(INTERNATIONAL_COLLABORATION_FACTOR,
                                                PostProcessFunction::getDecimalValue),
    PUBLICATION_CHANNEL_LEVEL_POINTS_FUNCTION(PUBLICATION_CHANNEL_LEVEL_POINTS, PostProcessFunction::getDecimalValue),
    PUBLICATION_IDENTIFIER_FUNCTION(PUBLICATION_IDENTIFIER, PostProcessFunction::getIdentifierFromUri),
    CONTRIBUTOR_IDENTIFIER_FUNCTION(CONTRIBUTOR_IDENTIFIER, PostProcessFunction::getIdentifierFromUri),
    APPROVAL_STATUS_FUNCTION(INSTITUTION_APPROVAL_STATUS, PostProcessFunction::postProcessApprovalStatus),
    INSTITUTION_IDENTIFIER_FUNCTION(INSTITUTION_ID,
                                    organizationUri -> getIdentifierAtIndex(organizationUri,
                                                                            Constants.INSTITUTION_ID_INDEX)),
    FACULTY_IDENTIFIER_FUNCTION(FACULTY_ID, organizationUri -> getIdentifierAtIndex(organizationUri,
                                                                                    Constants.FACULTY_ID_INDEX)),
    GROUP_IDENTIFIER_FUNCTION(GROUP_ID, organizationUri -> getIdentifierAtIndex(organizationUri,
                                                                                Constants.GROUP_ID_INDEX)),
    DEPARTMENT_IDENTIFIER_FUNCTION(DEPARTMENT_ID,
                                   organizationUri -> getIdentifierAtIndex(organizationUri,
                                                                           Constants.DEPARTMENT_ID_INDEX)),
    SCIENTIFIC_LEVEL_FUNCTION(PUBLICATION_CHANNEL_LEVEL, PostProcessFunction::postProcessScientificValue),
    PUBLICATION_CHANNEL_TYPE_FUNCTION(PUBLICATION_CHANNEL_TYPE, PostProcessFunction::getType),
    PUBLICATION_INSTANCE_FUNCTION(PUBLICATION_INSTANCE, PostProcessFunction::getType);

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

    private static String getType(String uri) {
        var split = uri.split("#");
        return split[split.length - 1];
    }

    private static String postProcessScientificValue(String scientificValue) {
        return switch (scientificValue) {
            case "LevelOne" -> "1";
            case "LevelTwo" -> "2";
            default -> scientificValue;
        };
    }

    private static String getIdentifierAtIndex(String organizationUri, int i) {
        return getIdentifierFromUri(organizationUri).split("\\.")[i];
    }

    private static String postProcessApprovalStatus(String rawValue) {
        return switch (rawValue) {
            case "Pending" -> "?";
            case "Approved" -> "J";
            case "Rejected" -> "N";
            default -> rawValue;
        };
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

    private static class Constants {

        public static final int INSTITUTION_ID_INDEX = 0;
        public static final int FACULTY_ID_INDEX = 1;
        public static final int DEPARTMENT_ID_INDEX = 2;
        public static final int GROUP_ID_INDEX = 3;
    }
}

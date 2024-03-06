package no.sikt.nva.data.report.api.fetch.testutils.generator;

import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.AUTHOR_COUNT;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.AUTHOR_INT;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.AUTHOR_SHARE_COUNT;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.DEPARTMENT_ID;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.FACULTY_ID;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.FIRST_NAME;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.GROUP_ID;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.INSTITUTION_APPROVAL_STATUS;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.INSTITUTION_ID;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.INTERNATIONAL_COLLABORATION_FACTOR;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.ISSN;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.IS_REPORTED;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.LANGUAGE;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.LAST_NAME;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PAGE_COUNT;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PAGE_FROM;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PAGE_TO;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.POINTS_FOR_AFFILIATION;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_LEVEL;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_NAME;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_TYPE;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_IDENTIFIER;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_INSTANCE;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_TITLE;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_TYPE_CHANNEL_LEVEL_POINTS;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.TOTAL_POINTS;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.publication.TestPublication.DELIMITER;
import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestApproval;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviCandidate;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviContributor;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviOrganization;
import no.sikt.nva.data.report.api.fetch.testutils.generator.publication.TestPublication;
import nva.commons.core.paths.UriWrapper;

public final class NviInstitutionStatusTestData {

    public static final List<String> NVI_INSTITUTION_STATUS_HEADERS = List.of(PUBLICATION_IDENTIFIER,
                                                                              INSTITUTION_APPROVAL_STATUS,
                                                                              PUBLICATION_INSTANCE,
                                                                              PUBLICATION_CHANNEL_TYPE,
                                                                              ISSN,
                                                                              PUBLICATION_CHANNEL_LEVEL,
                                                                              CONTRIBUTOR_IDENTIFIER,
                                                                              INSTITUTION_ID,
                                                                              FACULTY_ID,
                                                                              DEPARTMENT_ID,
                                                                              GROUP_ID,
                                                                              AUTHOR_COUNT,
                                                                              AUTHOR_INT,
                                                                              PUBLICATION_TYPE_CHANNEL_LEVEL_POINTS,
                                                                              LAST_NAME,
                                                                              FIRST_NAME,
                                                                              PUBLICATION_CHANNEL_NAME,
                                                                              PAGE_FROM,
                                                                              PAGE_TO,
                                                                              PAGE_COUNT,
                                                                              PUBLICATION_TITLE,
                                                                              LANGUAGE,
                                                                              IS_REPORTED,
                                                                              TOTAL_POINTS,
                                                                              INTERNATIONAL_COLLABORATION_FACTOR,
                                                                              AUTHOR_SHARE_COUNT,
                                                                              POINTS_FOR_AFFILIATION);

    public static String generateExpectedNviInstitutionResponse(TestNviContributor contributor,
                                                                TestNviCandidate candidate,
                                                                TestPublication publication) {
        var stringBuilder = new StringBuilder();
        contributor.affiliations()
            .forEach(affiliation -> generateExpectedNviInstitutionResponse(stringBuilder, contributor, affiliation,
                                                                           candidate, publication));
        return stringBuilder.toString();
    }

    private static void generateExpectedNviInstitutionResponse(StringBuilder stringBuilder,
                                                               TestNviContributor contributor,
                                                               TestNviOrganization affiliation,
                                                               TestNviCandidate candidate,
                                                               TestPublication publication) {
        var approval = findExpectedApproval(affiliation, candidate);
        stringBuilder.append(publication.getIdentifier()).append(DELIMITER)
            .append(approval.approvalStatus().getValue()).append(DELIMITER)
            .append(publication.getPublicationCategory()).append(DELIMITER)
            .append(publication.getChannel().getType()
                        .substring(publication.getChannel().getType().indexOf("#") + 1)).append(DELIMITER)
            .append(publication.getChannel().getPrintIssn()).append(DELIMITER)
            .append(publication.getChannel().getScientificValue()).append(DELIMITER)
            .append(UriWrapper.fromUri(contributor.id()).getLastPathElement()).append(DELIMITER)
            .append(affiliation.getOrganizationNumber()).append(DELIMITER)
            .append(affiliation.getSubUnitOneNumber()).append(DELIMITER)
            .append(affiliation.getSubUnitTwoNumber()).append(DELIMITER)
            .append(affiliation.getSubUnitThreeNumber()).append(DELIMITER)
            .append(AUTHOR_COUNT).append(DELIMITER)//TODO: Implement
            .append(AUTHOR_INT).append(DELIMITER)//TODO: Implement
            .append(candidate.totalPoints()).append(DELIMITER) //TODO: Check if correct
            .append(LAST_NAME).append(DELIMITER)//TODO: Implement
            .append(FIRST_NAME).append(DELIMITER)//TODO: Implement
            .append(publication.getChannel().getName()).append(DELIMITER)
            .append(PAGE_FROM).append(DELIMITER)//TODO: Implement
            .append(PAGE_TO).append(DELIMITER)//TODO: Implement
            .append(PAGE_COUNT).append(DELIMITER)//TODO: Implement
            .append(publication.getMainTitle()).append(DELIMITER)
            .append(LANGUAGE).append(DELIMITER)//TODO: Implement
            .append(candidate.reported() ? "Rapportert" : "Ikke rapportert").append(DELIMITER)
            .append(candidate.publicationTypeChannelLevelPoints()).append(DELIMITER) //TODO: Check if correct
            .append(candidate.internationalCollaborationFactor()).append(DELIMITER)
            .append(AUTHOR_SHARE_COUNT).append(DELIMITER)//TODO: Implement
            .append(POINTS_FOR_AFFILIATION).append(CRLF.getString());//TODO: Implement
    }

    private static TestApproval findExpectedApproval(TestNviOrganization affiliation, TestNviCandidate candidate) {
        return candidate.approvals().stream()
                   .filter(approval -> isApprovalForOrganization(approval, affiliation))
                   .findFirst()
                   .orElse(null);
    }

    private static boolean isApprovalForOrganization(TestApproval approval, TestNviOrganization organization) {
        return approval.institutionId()
                   .toString()
                   .equals(organization.getTopLevelOrganization());
    }
}

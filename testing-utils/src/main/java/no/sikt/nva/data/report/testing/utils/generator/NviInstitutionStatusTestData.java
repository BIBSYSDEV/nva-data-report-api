package no.sikt.nva.data.report.testing.utils.generator;

import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.DEPARTMENT_ID;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.FACULTY_ID;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.FIRST_NAME;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.GLOBAL_STATUS;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.GROUP_ID;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.INSTITUTION_APPROVAL_STATUS;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.INSTITUTION_ID;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.INTERNATIONAL_COLLABORATION_FACTOR;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.ISSN;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.LAST_NAME;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.POINTS_FOR_AFFILIATION;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_LEVEL;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_LEVEL_POINTS;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_NAME;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_TYPE;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.PUBLICATION_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.PUBLICATION_INSTANCE;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.PUBLICATION_TITLE;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.PUBLISHED_YEAR;
import static no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviInstitutionStatusHeaders.REPORTING_YEAR;
import static no.sikt.nva.data.report.testing.utils.generator.publication.TestPublication.DELIMITER;
import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.util.List;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestApproval;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestApprovalStatus;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestGlobalApprovalStatus;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviCandidate;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviContributor;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviOrganization;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestContributor;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestIdentity;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestLevel;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestPublication;
import nva.commons.core.paths.UriWrapper;

public final class NviInstitutionStatusTestData {

    public static final List<String> NVI_INSTITUTION_STATUS_HEADERS = List.of(REPORTING_YEAR,
                                                                              PUBLICATION_IDENTIFIER,
                                                                              PUBLISHED_YEAR,
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
                                                                              LAST_NAME,
                                                                              FIRST_NAME,
                                                                              PUBLICATION_CHANNEL_NAME,
                                                                              PUBLICATION_TITLE,
                                                                              GLOBAL_STATUS,
                                                                              PUBLICATION_CHANNEL_LEVEL_POINTS,
                                                                              INTERNATIONAL_COLLABORATION_FACTOR,
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
        var approval = getExpectedApproval(affiliation, candidate);
        var identity = getExpectedContributorIdentity(contributor, publication);
        stringBuilder.append(candidate.reportingPeriod()).append(DELIMITER)
            .append(publication.getIdentifier()).append(DELIMITER)
            .append(publication.getDate().year()).append(DELIMITER)
            .append(getExpectedApprovalStatusValue(approval.approvalStatus())).append(DELIMITER)
            .append(publication.getPublicationCategory()).append(DELIMITER)
            .append(publication.getChannel().getType()
                        .substring(publication.getChannel().getType().indexOf("#") + 1)).append(DELIMITER)
            .append(publication.getChannel().getPrintIssn()).append(DELIMITER)
            .append(getExpectedPublicationChannelLevel(publication)).append(DELIMITER)
            .append(UriWrapper.fromUri(contributor.id()).getLastPathElement()).append(DELIMITER)
            .append(affiliation.getOrganizationNumber()).append(DELIMITER)
            .append(affiliation.getSubUnitOneNumber()).append(DELIMITER)
            .append(affiliation.getSubUnitTwoNumber()).append(DELIMITER)
            .append(affiliation.getSubUnitThreeNumber()).append(DELIMITER)
            .append(identity.name()).append(DELIMITER)
            .append(identity.name()).append(DELIMITER)
            .append(publication.getChannel().getName()).append(DELIMITER)
            .append(publication.getMainTitle()).append(DELIMITER)
            .append(getExpectedApprovalStatusValue(candidate.globalApprovalStatus())).append(DELIMITER)
            .append(candidate.publicationTypeChannelLevelPoints()).append(DELIMITER)
            .append(candidate.internationalCollaborationFactor()).append(DELIMITER)
            .append(NviTestUtils.getExpectedPointsForAffiliation(affiliation, contributor, approval))
            .append(CRLF.getString());
    }

    private static TestIdentity getExpectedContributorIdentity(TestNviContributor contributor,
                                                               TestPublication publication) {
        return publication.getContributors().stream()
                   .map(TestContributor::getIdentity)
                   .filter(identity -> identity.uri().equals(contributor.id()))
                   .findFirst()
                   .orElse(null);
    }

    private static String getExpectedApprovalStatusValue(TestGlobalApprovalStatus approvalStatus) {
        return switch (approvalStatus) {
            case PENDING -> "?";
            case APPROVED -> "J";
            case REJECTED -> "N";
            case DISPUTE -> "T";
        };
    }

    private static String getExpectedApprovalStatusValue(TestApprovalStatus approvalStatus) {
        return switch (approvalStatus) {
            case NEW, PENDING -> "?";
            case APPROVED -> "J";
            case REJECTED -> "N";
        };
    }

    private static String getExpectedPublicationChannelLevel(TestPublication publication) {
        var level = TestLevel.parse(publication.getChannel().getScientificValue());
        return switch (level) {
            case LEVEL_ONE -> "1";
            case LEVEL_TWO -> "2";
        };
    }

    private static TestApproval getExpectedApproval(TestNviOrganization affiliation, TestNviCandidate candidate) {
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
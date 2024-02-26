package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.ApprovalGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.CandidateGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.PublicationDetailsGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.publication.OrganizationGenerator;
import nva.commons.core.paths.UriWrapper;
import org.apache.jena.rdf.model.Model;

public record TestNviCandidate(String identifier,
                               boolean isApplicable,
                               Instant modifiedDate,
                               TestPublicationDetails publicationDetails,
                               List<TestApproval> approvals,
                               BigDecimal totalPoints,
                               BigDecimal publicationTypeChannelLevelPoints,
                               int creatorShareCount,
                               BigDecimal internationalCollaborationFactor,
                               String reportedPeriod,
                               String globalApprovalStatus) {

    public static final String DELIMITER = ",";
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final int NVI_POINT_SCALE = 4;

    public static Builder builder() {
        return new Builder();
    }

    public String getExpectedNviResponse() {
        var stringBuilder = new StringBuilder();
        if (isApplicable) {
            publicationDetails().contributors()
                .forEach(contributor -> generateExpectedNviResponse(stringBuilder, contributor));
        } else {
            generateExpectedLinesForNonApplicableCandidate(stringBuilder);
        }
        return stringBuilder.toString();
    }

    public Model generateModel() {
        var publicationDetails = new PublicationDetailsGenerator(publicationDetails().id());
        addContributors(publicationDetails);
        var nviCandidate = getCandidateGenerator(publicationDetails);
        addApprovals(nviCandidate);
        return nviCandidate.build();
    }

    private static ApprovalGenerator getApprovalGenerator(TestApproval testApproval) {
        return testApproval.toModel()
                   .withApprovalStatus(testApproval.approvalStatus().getValue())
                   .withInstitutionId(
                       new OrganizationGenerator(testApproval.institutionId().toString()))
                   .withPoints(testApproval.points().toString());
    }

    private void generateExpectedLinesForNonApplicableCandidate(StringBuilder stringBuilder) {
        stringBuilder.append(publicationDetails().id())
            .append(DELIMITER).append(DELIMITER).append(DELIMITER).append(DELIMITER).append(DELIMITER).append(DELIMITER)
            .append(DELIMITER).append(DELIMITER).append(DELIMITER).append(DELIMITER).append(DELIMITER).append(DELIMITER)
            .append(DELIMITER).append(isApplicable()).append(CRLF.getString());
    }

    private void addApprovals(CandidateGenerator nviCandidate) {
        approvals().stream()
            .map(TestNviCandidate::getApprovalGenerator)
            .forEach(nviCandidate::withApproval);
    }

    private void addContributors(PublicationDetailsGenerator publicationDetails) {
        publicationDetails().contributors().stream()
            .map(TestNviContributor::toModel)
            .forEach(publicationDetails::withNviContributor);
    }

    private CandidateGenerator getCandidateGenerator(PublicationDetailsGenerator publicationDetails) {
        return new CandidateGenerator(identifier, modifiedDate.toString())
                   .withIsApplicable(isApplicable)
                   .withPublicationDetails(publicationDetails)
                   .withPoints(totalPoints.toString())
                   .withPublicationTypeChannelLevelPoints(publicationTypeChannelLevelPoints.toString())
                   .withCreatorShareCount(String.valueOf(creatorShareCount))
                   .withInternationalCollaborationFactor(internationalCollaborationFactor.toString())
                   .withReportedPeriod(reportedPeriod)
                   .withGlobalApprovalStatus(globalApprovalStatus);
    }

    private void generateExpectedNviResponse(StringBuilder stringBuilder, TestNviContributor contributor) {
        contributor.affiliations()
            .forEach(affiliation -> generateExpectedNviResponse(stringBuilder, contributor, affiliation));
    }

    private void generateExpectedNviResponse(StringBuilder stringBuilder, TestNviContributor contributor,
                                             TestNviOrganization affiliation) {
        var approval = findExpectedApproval(affiliation);
        var calculatedContributorPoints = calculateAffiliationPoints(affiliation);
        stringBuilder.append(publicationDetails().id()).append(DELIMITER)
            .append(extractLastPathElement(contributor.id())).append(DELIMITER)
            .append(affiliation.id()).append(DELIMITER)
            .append(approval.institutionId()).append(DELIMITER)
            .append(approval.points()).append(DELIMITER)
            .append(calculatedContributorPoints).append(DELIMITER)
            .append(approval.approvalStatus().getValue()).append(DELIMITER)
            .append(globalApprovalStatus).append(DELIMITER)
            .append(Objects.nonNull(reportedPeriod) ? reportedPeriod : "").append(DELIMITER)
            .append(totalPoints).append(DELIMITER)
            .append(publicationTypeChannelLevelPoints).append(DELIMITER)
            .append(creatorShareCount).append(DELIMITER)
            .append(internationalCollaborationFactor).append(DELIMITER)
            .append(isApplicable())
            .append(CRLF.getString());
    }

    private BigDecimal calculateAffiliationPoints(TestNviOrganization affiliation) {
        var topLevelOrganization = affiliation.getTopLevelOrganization();
        var contributorCount = countNumberOfContributorsWithTopLevelAffiliation(topLevelOrganization);
        var approvalPoints = Optional.ofNullable(findExpectedApproval(affiliation))
                                 .map(TestApproval::points)
                                 .orElseThrow();
        return approvalPoints.divide(BigDecimal.valueOf(contributorCount), ROUNDING_MODE)
                   .setScale(NVI_POINT_SCALE, ROUNDING_MODE);
    }

    private long countNumberOfContributorsWithTopLevelAffiliation(String topLevelOrganization) {
        return publicationDetails.contributors().stream()
                   .flatMap(contributor -> contributor.affiliations().stream())
                   .filter(affiliation -> affiliation.getTopLevelOrganization().equals(topLevelOrganization))
                   .count();
    }

    private TestApproval findExpectedApproval(TestNviOrganization affiliation) {
        return approvals().stream()
                   .filter(testApproval -> testApproval.institutionId()
                                               .toString()
                                               .equals(affiliation.getTopLevelOrganization()))
                   .findFirst()
                   .orElse(null);
    }

    private String extractLastPathElement(String uri) {
        return UriWrapper.fromUri(uri).getLastPathElement();
    }

    public static final class Builder {

        private String identifier;
        private boolean isApplicable;
        private Instant modifiedDate;
        private TestPublicationDetails publicationDetails;
        private List<TestApproval> approvals;
        private BigDecimal totalPoints;
        private BigDecimal publicationTypeChannelLevelPoints;
        private int creatorShareCount;
        private BigDecimal internationalCollaborationFactor;
        private String reportedPeriod;
        private String globalApprovalStatus;

        private Builder() {
        }

        public Builder withIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder withIsApplicable(boolean isApplicable) {
            this.isApplicable = isApplicable;
            return this;
        }

        public Builder withModifiedDate(Instant modifiedDate) {
            this.modifiedDate = modifiedDate;
            return this;
        }

        public Builder withPublicationDetails(TestPublicationDetails publicationDetails) {
            this.publicationDetails = publicationDetails;
            return this;
        }

        public Builder withApprovals(List<TestApproval> approvals) {
            this.approvals = approvals;
            return this;
        }

        public Builder withTotalPoints(BigDecimal totalPoints) {
            this.totalPoints = totalPoints;
            return this;
        }

        public Builder withPublicationTypeChannelLevelPoints(BigDecimal publicationTypeChannelLevelPoints) {
            this.publicationTypeChannelLevelPoints = publicationTypeChannelLevelPoints;
            return this;
        }

        public Builder withCreatorShareCount(int creatorShareCount) {
            this.creatorShareCount = creatorShareCount;
            return this;
        }

        public Builder withInternationalCollaborationFactor(BigDecimal internationalCollaborationFactor) {
            this.internationalCollaborationFactor = internationalCollaborationFactor;
            return this;
        }

        public Builder withReportedPeriod(String reportedPeriod) {
            this.reportedPeriod = reportedPeriod;
            return this;
        }

        public Builder withGlobalApprovalStatus(String globalApprovalStatus) {
            this.globalApprovalStatus = globalApprovalStatus;
            return this;
        }

        public TestNviCandidate build() {
            return new TestNviCandidate(identifier, isApplicable, modifiedDate, publicationDetails, approvals,
                                        totalPoints, publicationTypeChannelLevelPoints, creatorShareCount,
                                        internationalCollaborationFactor, reportedPeriod, globalApprovalStatus);
        }
    }
}

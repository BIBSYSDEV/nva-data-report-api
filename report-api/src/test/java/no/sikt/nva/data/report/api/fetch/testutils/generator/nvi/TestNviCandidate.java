package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviTestData.randomBigDecimal;
import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.ApprovalGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.CandidateGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.CreatorAffiliationPointsGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.InstitutionPointsGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.PublicationDetailsGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.ReportingPeriodGenerator;
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
                               boolean reported,
                               String reportingPeriod,
                               TestGlobalApprovalStatus globalApprovalStatus) {

    private static final String DELIMITER = ",";
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int NVI_POINT_SCALE = 4;

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

    private static boolean isForCreatorAndAffiliation(TestNviOrganization affiliation, TestNviContributor contributor,
                                                      TestCreatorAffiliationPoints creatorAffiliationPoints) {
        return creatorAffiliationPoints.creatorId().toString().equals(contributor.id()) &&
               creatorAffiliationPoints.affiliationId().toString().equals(affiliation.id());
    }

    private static void addAllCreatorAffiliationPoints(URI institutionId,
                                                       InstitutionPointsGenerator institutionPointsGenerator,
                                                       TestNviContributor creator) {
        creator.filterAffiliationsWithTopLevelOrg(institutionId.toString())
            .forEach(
                affiliation -> addCreatorAffiliationPoints(institutionPointsGenerator,
                                                           creator, affiliation));
    }

    private static void addCreatorAffiliationPoints(InstitutionPointsGenerator institutionPointsGenerator,
                                                    TestNviContributor creator,
                                                    TestNviOrganization affiliation) {
        institutionPointsGenerator.withCreatorAffiliationPoints(generateCreatorAffiliationPoints(creator, affiliation));
    }

    private static CreatorAffiliationPointsGenerator generateCreatorAffiliationPoints(TestNviContributor creator,
                                                                                      TestNviOrganization affiliation) {
        return new CreatorAffiliationPointsGenerator()
                   .withAffiliationId(affiliation.id())
                   .withCreatorId(creator.id())
                   .withPoints(randomBigDecimal().toString());
    }

    private ApprovalGenerator getApprovalGenerator(TestApproval testApproval) {
        var institutionPointsGenerator = new InstitutionPointsGenerator()
                                             .withPoints(testApproval.points().points().toString());
        addCreatorAffiliationPoints(testApproval.institutionId(), institutionPointsGenerator);
        return new ApprovalGenerator()
                           .withApprovalStatus(testApproval.approvalStatus().getValue())
                           .withInstitutionId(new OrganizationGenerator(testApproval.institutionId().toString()))
                           .withPoints(institutionPointsGenerator);
    }

    private void generateExpectedLinesForNonApplicableCandidate(StringBuilder stringBuilder) {
        stringBuilder.append(publicationDetails().id())
            .append(DELIMITER).append(DELIMITER).append(DELIMITER).append(DELIMITER).append(DELIMITER).append(DELIMITER)
            .append(DELIMITER).append(DELIMITER).append(DELIMITER).append(DELIMITER).append(DELIMITER).append(DELIMITER)
            .append(DELIMITER).append(isApplicable()).append(CRLF.getString());
    }

    private void addApprovals(CandidateGenerator nviCandidate) {
        approvals().stream()
            .map(this::getApprovalGenerator)
            .forEach(nviCandidate::withApproval);
    }

    private void addContributors(PublicationDetailsGenerator publicationDetails) {
        publicationDetails().contributors().stream()
            .map(TestNviContributor::toModel)
            .forEach(publicationDetails::withNviContributor);
    }

    private void addCreatorAffiliationPoints(URI institutionId, InstitutionPointsGenerator institutionPointsGenerator) {
        var nviCreators = publicationDetails.filterContributorsWithTopLevelOrg(institutionId.toString());
        nviCreators.forEach(
            creator -> addAllCreatorAffiliationPoints(institutionId, institutionPointsGenerator, creator));
    }

    private CandidateGenerator getCandidateGenerator(PublicationDetailsGenerator publicationDetails) {
        return new CandidateGenerator(identifier, modifiedDate.toString())
                   .withIsApplicable(isApplicable)
                   .withPublicationDetails(publicationDetails)
                   .withPoints(totalPoints.toString())
                   .withPublicationTypeChannelLevelPoints(publicationTypeChannelLevelPoints)
                   .withCreatorShareCount(String.valueOf(creatorShareCount))
                   .withInternationalCollaborationFactor(internationalCollaborationFactor)
                   .withReported(reported)
                   .withReportingPeriod(new ReportingPeriodGenerator().withYear(reportingPeriod))
                   .withGlobalApprovalStatus(globalApprovalStatus.getValue());
    }

    private void generateExpectedNviResponse(StringBuilder stringBuilder, TestNviContributor contributor) {
        contributor.affiliations()
            .forEach(affiliation -> generateExpectedNviResponse(stringBuilder, contributor, affiliation));
    }

    private void generateExpectedNviResponse(StringBuilder stringBuilder, TestNviContributor contributor,
                                             TestNviOrganization affiliation) {
        var approval = findExpectedApproval(affiliation);
        stringBuilder.append(publicationDetails().id()).append(DELIMITER)
            .append(extractLastPathElement(contributor.id())).append(DELIMITER)
            .append(affiliation.id()).append(DELIMITER)
            .append(approval.institutionId()).append(DELIMITER)
            .append(approval.points().points()).append(DELIMITER)
            .append(getExpectedPointsForAffiliation(affiliation, contributor)).append(DELIMITER)
            .append(approval.approvalStatus().getValue()).append(DELIMITER)
            .append(globalApprovalStatus.getValue()).append(DELIMITER)
            .append(reported ? reportingPeriod : "NotReported").append(DELIMITER)
            .append(totalPoints).append(DELIMITER)
            .append(publicationTypeChannelLevelPoints.stripTrailingZeros()).append(DELIMITER)
            .append(creatorShareCount).append(DELIMITER)
            .append(internationalCollaborationFactor.stripTrailingZeros()).append(DELIMITER)
            .append(isApplicable())
            .append(CRLF.getString());
    }

    private BigDecimal getExpectedPointsForAffiliation(TestNviOrganization affiliation,
                                                       TestNviContributor contributor) {
        var approval = findExpectedApproval(affiliation);
        return approval.points()
                   .creatorAffiliationPoints()
                   .stream()
                   .filter(pointsForAffiliation -> isForCreatorAndAffiliation(affiliation, contributor,
                                                                              pointsForAffiliation))
                   .findFirst()
                   .map(TestCreatorAffiliationPoints::points)
                   .map(points -> points.stripTrailingZeros().setScale(NVI_POINT_SCALE, ROUNDING_MODE))
                   .orElseThrow();
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
        private boolean reported;
        private String reportingPeriod;
        private TestGlobalApprovalStatus globalApprovalStatus;

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

        public Builder withReported(boolean reported) {
            this.reported = reported;
            return this;
        }

        public Builder withReportingPeriod(String reportingPeriod) {
            this.reportingPeriod = reportingPeriod;
            return this;
        }

        public Builder withGlobalApprovalStatus(TestGlobalApprovalStatus globalApprovalStatus) {
            this.globalApprovalStatus = globalApprovalStatus;
            return this;
        }

        public TestNviCandidate build() {
            return new TestNviCandidate(identifier, isApplicable, modifiedDate, publicationDetails, approvals,
                                        totalPoints, publicationTypeChannelLevelPoints, creatorShareCount,
                                        internationalCollaborationFactor, reported, reportingPeriod,
                                        globalApprovalStatus);
        }
    }
}

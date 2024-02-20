package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.time.Instant;
import java.util.List;
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
                               List<TestApproval> approvals) {

    public static final String DELIMITER = ",";

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
            .append(DELIMITER)
            .append(DELIMITER)
            .append(DELIMITER)
            .append(DELIMITER)
            .append(DELIMITER)
            .append(DELIMITER)
            .append(isApplicable())
            .append(CRLF.getString());
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
                   .withPublicationDetails(publicationDetails);
    }

    private void generateExpectedNviResponse(StringBuilder stringBuilder, TestNviContributor contributor) {
        contributor.affiliations()
            .forEach(affiliation -> generateExpectedNviResponse(stringBuilder, contributor, affiliation));
    }

    private void generateExpectedNviResponse(StringBuilder stringBuilder, TestNviContributor contributor,
                                             TestNviOrganization affiliation) {
        var approval = generateExpectedApprovals(affiliation);
        stringBuilder.append(publicationDetails().id()).append(DELIMITER)
            .append(extractLastPathElement(contributor.id())).append(DELIMITER)
            .append(affiliation.id()).append(DELIMITER)
            .append(approval.institutionId()).append(DELIMITER)
            .append(approval.points()).append(DELIMITER)
            .append(approval.approvalStatus().getValue()).append(DELIMITER)
            .append(isApplicable())
            .append(CRLF.getString());
    }

    private TestApproval generateExpectedApprovals(TestNviOrganization affiliation) {
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

        public TestNviCandidate build() {
            return new TestNviCandidate(identifier, isApplicable, modifiedDate, publicationDetails, approvals);
        }
    }
}

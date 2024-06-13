package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import java.net.URI;
import java.util.Set;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.ApprovalGenerator;

public record TestApproval(URI institutionId,
                           TestApprovalStatus approvalStatus,
                           Set<String> involvedOrganizations,
                           TestInstitutionPoints points) {

    public static Builder builder() {
        return new Builder();
    }

    public ApprovalGenerator toModel() {
        return new ApprovalGenerator();
    }

    public static final class Builder {

        private URI institutionId;
        private TestApprovalStatus approvalStatus;
        private Set<String> involvedOrganizations;
        private TestInstitutionPoints points;

        private Builder() {
        }

        public Builder withInstitutionId(URI institutionId) {
            this.institutionId = institutionId;
            return this;
        }

        public Builder withApprovalStatus(TestApprovalStatus approvalStatus) {
            this.approvalStatus = approvalStatus;
            return this;
        }

        public Builder withInvolvedOrganizations(Set<String> involvedOrganizations) {
            this.involvedOrganizations = involvedOrganizations;
            return this;
        }

        public Builder withPoints(TestInstitutionPoints points) {
            this.points = points;
            return this;
        }

        public TestApproval build() {
            return new TestApproval(institutionId, approvalStatus, involvedOrganizations, points);
        }
    }
}
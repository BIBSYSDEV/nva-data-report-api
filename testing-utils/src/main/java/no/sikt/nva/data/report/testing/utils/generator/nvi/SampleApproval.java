package no.sikt.nva.data.report.testing.utils.generator.nvi;

import java.net.URI;
import java.util.Set;
import no.sikt.nva.data.report.testing.utils.generator.model.nvi.ApprovalGenerator;

public record SampleApproval(URI institutionId,
                             SampleApprovalStatus approvalStatus,
                             Set<String> involvedOrganizations,
                             SampleInstitutionPoints points) {

    public static Builder builder() {
        return new Builder();
    }

    public ApprovalGenerator toModel() {
        return new ApprovalGenerator();
    }

    public static final class Builder {

        private URI institutionId;
        private SampleApprovalStatus approvalStatus;
        private Set<String> involvedOrganizations;
        private SampleInstitutionPoints points;

        private Builder() {
        }

        public Builder withInstitutionId(URI institutionId) {
            this.institutionId = institutionId;
            return this;
        }

        public Builder withApprovalStatus(SampleApprovalStatus approvalStatus) {
            this.approvalStatus = approvalStatus;
            return this;
        }

        public Builder withInvolvedOrganizations(Set<String> involvedOrganizations) {
            this.involvedOrganizations = involvedOrganizations;
            return this;
        }

        public Builder withPoints(SampleInstitutionPoints points) {
            this.points = points;
            return this;
        }

        public SampleApproval build() {
            return new SampleApproval(institutionId, approvalStatus, involvedOrganizations, points);
        }
    }
}
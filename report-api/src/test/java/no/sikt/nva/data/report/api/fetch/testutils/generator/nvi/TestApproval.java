package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import java.math.BigDecimal;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.ApprovalGenerator;

public record TestApproval(String institutionId,
                           ApprovalStatus approvalStatus,
                           BigDecimal points) {

    public static Builder builder() {
        return new Builder();
    }

    public ApprovalGenerator toModel() {
        return new ApprovalGenerator();
    }

    public enum ApprovalStatus {

        PENDING("Pending"),
        APPROVED("Approved"),
        REJECTED("Rejected");

        private final String value;

        ApprovalStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static final class Builder {

        private String institutionId;
        private ApprovalStatus approvalStatus;
        private BigDecimal points;

        private Builder() {
        }

        public Builder withInstitutionId(String institutionId) {
            this.institutionId = institutionId;
            return this;
        }

        public Builder withApprovalStatus(ApprovalStatus approvalStatus) {
            this.approvalStatus = approvalStatus;
            return this;
        }

        public Builder withPoints(BigDecimal points) {
            this.points = points;
            return this;
        }

        public TestApproval build() {
            return new TestApproval(institutionId, approvalStatus, points);
        }
    }
}

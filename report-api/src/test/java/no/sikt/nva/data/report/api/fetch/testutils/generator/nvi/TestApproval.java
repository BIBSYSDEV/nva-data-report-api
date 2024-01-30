package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import java.math.BigDecimal;

public record TestApproval(String institutionId,
                           ApprovalStatus approvalStatus,
                           BigDecimal points) {

    public static Builder builder() {
        return new Builder();
    }

    private enum ApprovalStatus {

        PENDING("Pending"),
        APPROVED("Approved"),
        REJECTED("Rejected");

        ApprovalStatus(String value) {
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

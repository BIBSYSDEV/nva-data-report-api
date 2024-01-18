package no.sikt.nva.data.report.api.etl.testutils.model.nvi;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import nva.commons.core.JacocoGenerated;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
public record Approval(String id,
                       Map<String, String> labels,
                       String approvalStatus,
                       String assignee) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;
        private Map<String, String> labels;
        private String approvalStatus;
        private String assignee;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withLabels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public Builder withApprovalStatus(String approvalStatus) {
            this.approvalStatus = approvalStatus;
            return this;
        }

        @JacocoGenerated
        public Builder withAssignee(String assignee) {
            this.assignee = assignee;
            return this;
        }

        public Approval build() {
            return new Approval(id, labels, approvalStatus, assignee);
        }
    }
}

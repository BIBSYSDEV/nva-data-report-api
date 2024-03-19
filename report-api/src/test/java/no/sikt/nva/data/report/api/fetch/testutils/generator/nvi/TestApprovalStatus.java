package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TestApprovalStatus {
    APPROVED("Approved"), PENDING("Pending"), REJECTED("Rejected");

    @JsonValue
    private final String value;

    TestApprovalStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

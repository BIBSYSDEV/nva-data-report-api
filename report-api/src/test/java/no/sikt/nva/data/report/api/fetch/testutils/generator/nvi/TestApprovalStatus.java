package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum TestApprovalStatus {
    APPROVED("Approved"), PENDING("Pending"), REJECTED("Rejected");

    @JsonValue
    private final String value;

    TestApprovalStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TestApprovalStatus parse(String value) {
        return Arrays.stream(TestApprovalStatus.values())
                   .filter(status -> status.getValue().equalsIgnoreCase(value))
                   .findFirst()
                   .orElseThrow();
    }

    public String getValue() {
        return value;
    }
}

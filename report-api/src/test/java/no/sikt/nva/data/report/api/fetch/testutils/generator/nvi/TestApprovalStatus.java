package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

public enum TestApprovalStatus {
    NEW("New"), APPROVED("Approved"), PENDING("Pending"), REJECTED("Rejected");
    private final String value;

    TestApprovalStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

package no.sikt.nva.data.report.testing.utils.generator.nvi;

public enum SampleApprovalStatus {
    NEW("New"), APPROVED("Approved"), PENDING("Pending"), REJECTED("Rejected");
    private final String value;

    SampleApprovalStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

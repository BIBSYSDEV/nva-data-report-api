package no.sikt.nva.data.report.testing.utils.generator.nvi;

public enum TestGlobalApprovalStatus {

    APPROVED("Approved"), PENDING("Pending"), REJECTED("Rejected"), DISPUTE("Dispute");
    private final String value;

    TestGlobalApprovalStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

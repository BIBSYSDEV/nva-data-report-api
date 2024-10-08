package no.sikt.nva.data.report.testing.utils.generator.nvi;

public enum SampleGlobalApprovalStatus {

    APPROVED("Approved"), PENDING("Pending"), REJECTED("Rejected"), DISPUTE("Dispute");
    private final String value;

    SampleGlobalApprovalStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

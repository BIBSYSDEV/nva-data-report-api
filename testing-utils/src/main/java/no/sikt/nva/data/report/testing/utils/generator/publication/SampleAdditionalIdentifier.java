package no.sikt.nva.data.report.testing.utils.generator.publication;

public class SampleAdditionalIdentifier {

    private String sourceName;
    private String value;

    public SampleAdditionalIdentifier() {
    }

    public SampleAdditionalIdentifier withSourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    public SampleAdditionalIdentifier withValue(String value) {
        this.value = value;
        return this;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getValue() {
        return value;
    }
}

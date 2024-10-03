package no.sikt.nva.data.report.testing.utils.generator.publication;

public class SampleAdditionalIdentifier {

    private String sourceName;
    private String value;
    private String type;

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

    public SampleAdditionalIdentifier withType(String type) {
        this.type = type;
        return this;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}

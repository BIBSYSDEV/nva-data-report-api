package no.sikt.nva.data.report.testing.utils.generator.publication;

import no.sikt.nva.data.report.testing.utils.generator.model.publication.FundingGenerator;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.FundingSourceGenerator;

public class TestFunding {

    private String fundingSource;
    private String id;
    private String name;

    public TestFunding() {

    }

    public TestFunding withFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
        return this;
    }

    public TestFunding withId(String fundingIdentifier) {
        this.id = fundingIdentifier;
        return this;
    }

    public TestFunding withName(String fundingName) {
        this.name = fundingName;
        return this;
    }

    public FundingGenerator toModel() {
        return new FundingGenerator()
                   .withIdentifier(id)
                   .withSource(new FundingSourceGenerator().withLabel(name, "en").withIdentifier(fundingSource));
    }

    public String getFundingSource() {
        return fundingSource;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

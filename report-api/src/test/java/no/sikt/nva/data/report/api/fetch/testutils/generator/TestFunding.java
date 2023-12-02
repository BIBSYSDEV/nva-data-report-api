package no.sikt.nva.data.report.api.fetch.testutils.generator;

import no.sikt.nva.data.report.api.fetch.testutils.generator.model.FundingGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.FundingSourceGenerator;

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
                   .withLabel(name, "en")
                   .withSource(new FundingSourceGenerator().withIdentifier(fundingSource));
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

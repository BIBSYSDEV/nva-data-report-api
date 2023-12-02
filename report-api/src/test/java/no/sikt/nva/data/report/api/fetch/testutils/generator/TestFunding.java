package no.sikt.nva.data.report.api.fetch.testutils.generator;

import no.sikt.nva.data.report.api.fetch.testutils.generator.model.FundingGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.FundingSourceGenerator;

public class TestFunding {

    private String fundingSource;
    private String fundingIdentifier;
    private String fundingName;

    public TestFunding() {

    }

    public TestFunding withFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
        return this;
    }

    public TestFunding withFundingIdentifier(String fundingIdentifier) {
        this.fundingIdentifier = fundingIdentifier;
        return this;
    }

    public TestFunding withFundingName(String fundingName) {
        this.fundingName = fundingName;
        return this;
    }

    public FundingGenerator toModel() {
        return new FundingGenerator()
                   .withIdentifier(fundingIdentifier)
                   .withLabel(fundingName, "en")
                   .withSource(new FundingSourceGenerator().withIdentifier(fundingSource));
    }
}

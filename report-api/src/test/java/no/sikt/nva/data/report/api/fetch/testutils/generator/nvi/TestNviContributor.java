package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import java.util.List;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.NviContributorGenerator;

public record TestNviContributor(String id,
                                 List<TestNviOrganization> affiliations) {

    public NviContributorGenerator toModel() {
        var contributor = new NviContributorGenerator(id);
        affiliations.stream().map(TestNviOrganization::toModel)
            .forEach(contributor::withAffiliation);
        return contributor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;
        private List<TestNviOrganization> affiliations;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withAffiliations(List<TestNviOrganization> affiliations) {
            this.affiliations = affiliations;
            return this;
        }

        public TestNviContributor build() {
            return new TestNviContributor(id, affiliations);
        }
    }
}

package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import java.util.List;

public record TestNviContributor(String id,
                                 boolean isNviContributor,
                                 List<TestAffiliation> affiliations) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;
        private boolean isNviContributor;
        private List<TestAffiliation> affiliations;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withIsNviContributor(boolean isNviContributor) {
            this.isNviContributor = isNviContributor;
            return this;
        }

        public Builder withAffiliations(List<TestAffiliation> affiliations) {
            this.affiliations = affiliations;
            return this;
        }

        public TestNviContributor build() {
            return new TestNviContributor(id, isNviContributor, affiliations);
        }
    }
}

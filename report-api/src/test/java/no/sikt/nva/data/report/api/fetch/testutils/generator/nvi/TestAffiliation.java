package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.NviAffiliationGenerator;

public record TestAffiliation(String id,
                              boolean isNviAffiliation) {

    public NviAffiliationGenerator toModel() {
        return new NviAffiliationGenerator(id);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;
        private boolean isNviAffiliation;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withIsNviAffiliation(boolean isNviAffiliation) {
            this.isNviAffiliation = isNviAffiliation;
            return this;
        }

        public TestAffiliation build() {
            return new TestAffiliation(id, isNviAffiliation);
        }
    }
}

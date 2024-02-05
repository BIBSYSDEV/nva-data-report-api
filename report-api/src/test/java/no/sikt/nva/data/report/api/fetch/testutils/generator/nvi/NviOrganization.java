package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.NviOrganizationGenerator;

public record NviOrganization(String id) {

    public NviOrganizationGenerator toModel() {
        return new NviOrganizationGenerator(id);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public NviOrganization build() {
            return new NviOrganization(id);
        }
    }
}

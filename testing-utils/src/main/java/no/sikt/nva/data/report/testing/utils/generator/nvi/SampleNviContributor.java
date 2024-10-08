package no.sikt.nva.data.report.testing.utils.generator.nvi;

import java.util.List;
import no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviContributorGenerator;

public record SampleNviContributor(String id,
                                   List<SampleNviOrganization> affiliations) {

    public List<SampleNviOrganization> filterAffiliationsWithTopLevelOrg(String institutionId) {
        return affiliations.stream()
                   .filter(affiliation -> hasTopLevelOrg(affiliation, institutionId))
                   .toList();
    }

    private static boolean hasTopLevelOrg(SampleNviOrganization organization, String topLevelOrgId) {
        return organization.getTopLevelOrganization().equals(topLevelOrgId);
    }

    public NviContributorGenerator toModel() {
        var contributor = new NviContributorGenerator(id);
        affiliations.stream().map(SampleNviOrganization::toModel)
            .forEach(contributor::withAffiliation);
        return contributor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;
        private List<SampleNviOrganization> affiliations;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withAffiliations(List<SampleNviOrganization> affiliations) {
            this.affiliations = affiliations;
            return this;
        }

        public SampleNviContributor build() {
            return new SampleNviContributor(id, affiliations);
        }
    }
}

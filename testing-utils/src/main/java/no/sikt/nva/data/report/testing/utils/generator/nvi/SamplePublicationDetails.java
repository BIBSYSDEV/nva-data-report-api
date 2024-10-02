package no.sikt.nva.data.report.testing.utils.generator.nvi;

import java.util.List;

public record SamplePublicationDetails(String id, List<SampleNviContributor> contributors) {

    public static Builder builder() {
        return new Builder();
    }

    public List<SampleNviContributor> filterContributorsWithTopLevelOrg(String institutionId) {
        return contributors.stream()
                   .filter(contributor -> isAffiliatedToTopLevelOrg(contributor, institutionId))
                   .toList();
    }

    private static boolean isAffiliatedToTopLevelOrg(SampleNviContributor contributor, String institutionId) {
        return contributor.affiliations()
                   .stream()
                   .anyMatch(affiliation -> hasTopLevelOrg(affiliation, institutionId));
    }

    private static boolean hasTopLevelOrg(SampleNviOrganization affiliation, String institutionId) {
        return affiliation.getTopLevelOrganization().equals(institutionId);
    }

    public static final class Builder {

        private String id;
        private List<SampleNviContributor> contributors;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withContributors(List<SampleNviContributor> contributors) {
            this.contributors = contributors;
            return this;
        }

        public SamplePublicationDetails build() {
            return new SamplePublicationDetails(id, contributors);
        }
    }
}

package no.sikt.nva.data.report.testing.utils.generator.nvi;

import java.util.List;

public record TestPublicationDetails(String id, List<TestNviContributor> contributors) {

    public static Builder builder() {
        return new Builder();
    }

    public List<TestNviContributor> filterContributorsWithTopLevelOrg(String institutionId) {
        return contributors.stream()
                   .filter(contributor -> isAffiliatedToTopLevelOrg(contributor, institutionId))
                   .toList();
    }

    private static boolean isAffiliatedToTopLevelOrg(TestNviContributor contributor, String institutionId) {
        return contributor.affiliations()
                   .stream()
                   .anyMatch(affiliation -> hasTopLevelOrg(affiliation, institutionId));
    }

    private static boolean hasTopLevelOrg(TestNviOrganization affiliation, String institutionId) {
        return affiliation.getTopLevelOrganization().equals(institutionId);
    }

    public static final class Builder {

        private String id;
        private List<TestNviContributor> contributors;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withContributors(List<TestNviContributor> contributors) {
            this.contributors = contributors;
            return this;
        }

        public TestPublicationDetails build() {
            return new TestPublicationDetails(id, contributors);
        }
    }
}

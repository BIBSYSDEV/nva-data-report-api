package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import java.util.List;

public record TestPublicationDetails(String id, List<TestNviContributor> contributors) {

      public static Builder builder() {
          return new Builder();
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

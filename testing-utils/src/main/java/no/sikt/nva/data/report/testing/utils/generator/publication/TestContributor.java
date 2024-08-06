package no.sikt.nva.data.report.testing.utils.generator.publication;

import java.util.List;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.ContributorGenerator;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.RoleGenerator;

public class TestContributor {
    private String contributorRole;
    private List<TestOrganization> affiliations;
    private String contributorSequenceNumber;
    private TestIdentity identity;

    public TestContributor() {

    }

    public TestContributor withAffiliations(List<TestOrganization> affiliations) {
        this.affiliations = affiliations;
        return this;
    }

    public TestContributor withContributorSequenceNo(String contributorSequenceNumber) {
        this.contributorSequenceNumber = contributorSequenceNumber;
        return this;
    }

    public TestContributor withContributorRole(String contributorRole) {
        this.contributorRole = contributorRole;
        return this;
    }

    public List<TestOrganization> getAffiliations() {
        return affiliations;
    }

    public ContributorGenerator toModel() {
        var contributor = new ContributorGenerator()
                              .withIdentity(identity.toModel())
                              .withSequence(contributorSequenceNumber)
                              .withRole(new RoleGenerator(contributorRole));
        affiliations.stream().map(TestOrganization::toModel)
            .forEach(contributor::withAffiliation);
        return contributor;
    }

    public TestContributor withIdentity(TestIdentity identity) {
        this.identity = identity;
        return this;
    }

    public TestIdentity getIdentity() {
        return identity;
    }

    public String getSequenceNumber() {
        return contributorSequenceNumber;
    }

    public String role() {
        return contributorRole;
    }
}

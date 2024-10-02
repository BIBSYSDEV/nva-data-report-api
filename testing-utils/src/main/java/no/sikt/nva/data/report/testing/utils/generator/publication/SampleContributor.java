package no.sikt.nva.data.report.testing.utils.generator.publication;

import java.util.List;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.ContributorGenerator;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.RoleGenerator;

public class SampleContributor {
    private String contributorRole;
    private List<SampleOrganization> affiliations;
    private String contributorSequenceNumber;
    private SampleIdentity identity;

    public SampleContributor() {

    }

    public SampleContributor withAffiliations(List<SampleOrganization> affiliations) {
        this.affiliations = affiliations;
        return this;
    }

    public SampleContributor withContributorSequenceNo(String contributorSequenceNumber) {
        this.contributorSequenceNumber = contributorSequenceNumber;
        return this;
    }

    public SampleContributor withContributorRole(String contributorRole) {
        this.contributorRole = contributorRole;
        return this;
    }

    public List<SampleOrganization> getAffiliations() {
        return affiliations;
    }

    public ContributorGenerator toModel() {
        var contributor = new ContributorGenerator()
                              .withIdentity(identity.toModel())
                              .withSequence(contributorSequenceNumber)
                              .withRole(new RoleGenerator(contributorRole));
        affiliations.stream().map(SampleOrganization::toModel)
            .forEach(contributor::withAffiliation);
        return contributor;
    }

    public SampleContributor withIdentity(SampleIdentity identity) {
        this.identity = identity;
        return this;
    }

    public SampleIdentity getIdentity() {
        return identity;
    }

    public String getSequenceNumber() {
        return contributorSequenceNumber;
    }

    public String role() {
        return contributorRole;
    }
}

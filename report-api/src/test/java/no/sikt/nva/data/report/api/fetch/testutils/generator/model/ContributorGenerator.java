package no.sikt.nva.data.report.api.fetch.testutils.generator.model;

import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class ContributorGenerator extends TripleBasedBuilder {

    private static final Property SEQUENCE_NUMBER = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "sequence");
    private static final Property ROLE = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "role");
    private final Model model;
    private static final Resource CONTRIBUTOR = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "Contributor");
    private static final Property IDENTITY = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "identity");
    private static final Property AFFILIATION = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "affiliation");
    private final Resource subject;

    public ContributorGenerator() {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource();
        model.add(subject, TYPE, CONTRIBUTOR);
    }

    public ContributorGenerator withIdentity(IdentityGenerator identity) {
        model.add(subject, IDENTITY, identity.getSubject());
        model.add(identity.build());
        return this;
    }

    public ContributorGenerator withAffiliation(OrganizationGenerator affiliation) {
        model.add(subject, AFFILIATION, affiliation.getSubject());
        model.add(affiliation.build());
        return this;
    }

    public ContributorGenerator withRole(RoleGenerator role) {
        model.add(subject, ROLE, role.getSubject());
        model.add(role.build());
        return this;
    }

    public ContributorGenerator withSequence(String sequenceNumber) {
        model.add(subject, SEQUENCE_NUMBER, model.createLiteral(sequenceNumber));
        return this;
    }

    @Override
    public Model build() {
        return model;
    }

    @Override
    public Resource getSubject() {
        return subject;
    }
}

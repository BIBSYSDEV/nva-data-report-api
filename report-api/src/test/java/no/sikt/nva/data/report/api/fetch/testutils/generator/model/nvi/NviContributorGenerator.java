package no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi;

import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class NviContributorGenerator extends TripleBasedBuilder {

    private static final Property CONTRIBUTOR = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "NviContributor");
    private static final Property AFFILIATION = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "affiliation");
    private final Model model;
    private final Resource subject;

    public NviContributorGenerator(String id) {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource(id);
        model.add(subject, TYPE, CONTRIBUTOR);
    }

    public NviContributorGenerator withAffiliation(NviOrganizationGenerator affiliation) {
        model.add(subject, AFFILIATION, affiliation.getSubject());
        model.add(affiliation.build());
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

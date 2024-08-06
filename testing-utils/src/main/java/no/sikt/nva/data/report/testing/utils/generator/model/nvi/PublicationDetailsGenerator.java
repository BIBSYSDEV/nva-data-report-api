package no.sikt.nva.data.report.testing.utils.generator.model.nvi;

import no.sikt.nva.data.report.testing.utils.generator.Constants;
import no.sikt.nva.data.report.testing.utils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class PublicationDetailsGenerator extends TripleBasedBuilder {

    private static final Resource PUBLICATION_DETAILS = new ResourceImpl(
        Constants.ONTOLOGY_BASE_URI + "PublicationDetails");
    private static final Property CONTRIBUTOR = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "contributor");
    private final Model model;
    private final Resource subject;

    public PublicationDetailsGenerator(String id) {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource(id);
        model.add(subject, TYPE, PUBLICATION_DETAILS);
    }

    public PublicationDetailsGenerator withNviContributor(NviContributorGenerator contributor) {
        model.add(subject, CONTRIBUTOR, contributor.getSubject());
        model.add(contributor.build());
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

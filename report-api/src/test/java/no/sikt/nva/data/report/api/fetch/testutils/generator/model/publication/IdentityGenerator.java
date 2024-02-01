package no.sikt.nva.data.report.api.fetch.testutils.generator.model.publication;

import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class IdentityGenerator extends TripleBasedBuilder {

    private static final Property NAME = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "name");
    public final Model model;
    private static final Resource IDENTITY = new ResourceImpl(Constants.ONTOLOGY_BASE_URI, "Identity");
    private final Resource subject;

    public IdentityGenerator(String id, String name) {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource(id);
        model.add(this.subject, TYPE, IDENTITY);
        model.add(subject, NAME, model.createLiteral(name));
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

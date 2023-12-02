package no.sikt.nva.data.report.api.fetch.testutils.generator.model;

import static no.sikt.nva.data.report.api.fetch.testutils.generator.Constants.ONTOLOGY_BASE_URI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class ReferenceGenerator extends TripleBasedBuilder {

    public final Model model;
    private static final Resource REFERENCE = new ResourceImpl(ONTOLOGY_BASE_URI + "Reference");
    private static final Property PUBLICATION_CONTEXT = new PropertyImpl(ONTOLOGY_BASE_URI, "publicationContext");
    private static final Property PUBLICATION_INSTANCE = new PropertyImpl(ONTOLOGY_BASE_URI, "publicationInstance");
    private final Resource subject;

    public ReferenceGenerator() {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource();
        model.add(subject, TYPE, REFERENCE);
    }

    public ReferenceGenerator withPublicationContext(PublicationContext publicationContext) {
        model.add(subject, PUBLICATION_CONTEXT, publicationContext.getSubject());
        model.add(publicationContext.build());
        return this;
    }

    public ReferenceGenerator withPublicationInstance(PublicationInstanceGenerator publicationInstance) {
        model.add(subject, PUBLICATION_INSTANCE, publicationInstance.getSubject());
        model.add(publicationInstance.build());
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

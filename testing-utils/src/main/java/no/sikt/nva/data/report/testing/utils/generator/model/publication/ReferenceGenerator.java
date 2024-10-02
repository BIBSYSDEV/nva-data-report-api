package no.sikt.nva.data.report.testing.utils.generator.model.publication;

import no.sikt.nva.data.report.testing.utils.generator.Constants;
import no.sikt.nva.data.report.testing.utils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class ReferenceGenerator extends TripleBasedBuilder {

    private static final Resource REFERENCE = new ResourceImpl(Constants.ONTOLOGY_BASE_URI + "Reference");
    private static final Property PUBLICATION_CONTEXT = new PropertyImpl(Constants.ONTOLOGY_BASE_URI,
                                                                         "publicationContext");
    private static final Property PUBLICATION_INSTANCE = new PropertyImpl(Constants.ONTOLOGY_BASE_URI,
                                                                          "publicationInstance");
    public final Model model;
    private final Resource subject;

    public ReferenceGenerator() {
        super();
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

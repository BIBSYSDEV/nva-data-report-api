package no.sikt.nva.data.report.api.fetch.testutils.generator.model.publication;

import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class EntityDescriptionGenerator extends TripleBasedBuilder {

    private final Model model;
    private static final Property CONTRIBUTOR = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "contributor");
    private static final Property MAIN_TITLE = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "mainTitle");
    private static final Resource ENTITY_DESCRIPTION = new ResourceImpl(
        Constants.ONTOLOGY_BASE_URI + "EntityDescription");
    private static final Property REFERENCE = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "reference");
    private static final Property PUBLICATION_DATE = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "publicationDate");
    private final Resource subject;

    public EntityDescriptionGenerator() {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource();
        model.add(subject, TYPE, ENTITY_DESCRIPTION);
    }

    public EntityDescriptionGenerator withMainTitle(String mainTitle) {
        model.add(subject, MAIN_TITLE, model.createLiteral(mainTitle));
        return this;
    }

    public EntityDescriptionGenerator withContributor(ContributorGenerator contributor) {
        model.add(subject, CONTRIBUTOR, contributor.getSubject());
        model.add(contributor.build());
        return this;
    }

    public EntityDescriptionGenerator withReference(ReferenceGenerator reference) {
        model.add(subject, REFERENCE, reference.getSubject());
        model.add(reference.build());
        return this;
    }

    public EntityDescriptionGenerator withPublicationDate(PublicationDateGenerator publicationDate) {
        model.add(subject, PUBLICATION_DATE, publicationDate.getSubject());
        model.add(publicationDate.build());
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

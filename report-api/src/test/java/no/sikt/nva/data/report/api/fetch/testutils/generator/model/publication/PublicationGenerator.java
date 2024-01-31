package no.sikt.nva.data.report.api.fetch.testutils.generator.model.publication;

import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import org.apache.jena.datatypes.xsd.impl.XSDDateTimeType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class PublicationGenerator extends TripleBasedBuilder {

    private final Model model;
    public static final Resource PUBLICATION = new ResourceImpl(Constants.ONTOLOGY_BASE_URI + "Publication");
    private static final Property MODIFIED_DATE = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "modifiedDate");
    private static final Property ENTITY_DESCRIPTION = new PropertyImpl(
        Constants.ONTOLOGY_BASE_URI + "entityDescription");
    private static final Property FUNDING = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "funding");
    public static final Property IDENTIFIER = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "identifier");

    private final Resource subject;

    public PublicationGenerator(String identifier, String modifiedDate) {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource(Constants.PUBLICATION_BASE_URI + identifier);
        model.add(subject, TYPE, PUBLICATION);
        model.add(subject, IDENTIFIER, model.createLiteral(identifier));
        model.add(subject, MODIFIED_DATE, model.createTypedLiteral(modifiedDate, XSDDateTimeType.XSDdateTime));
    }

    public PublicationGenerator withEntityDescription(EntityDescriptionGenerator entityDescription) {
        model.add(subject, ENTITY_DESCRIPTION, entityDescription.getSubject());
        model.add(entityDescription.build());
        return this;
    }

    public PublicationGenerator withFunding(FundingGenerator funding) {
        model.add(subject, FUNDING, funding.getSubject());
        model.add(funding.build());
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

package no.sikt.nva.data.report.testing.utils.generator.model.publication;

import static no.sikt.nva.data.report.testing.utils.generator.model.publication.PublicationGenerator.IDENTIFIER;
import java.util.UUID;
import no.sikt.nva.data.report.testing.utils.generator.Constants;
import no.sikt.nva.data.report.testing.utils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class PublisherGenerator extends TripleBasedBuilder implements PublicationContext {

    public final Model model;
    private static final Resource Publisher = new ResourceImpl(Constants.ONTOLOGY_BASE_URI + "Publisher");
    private static final Property SCIENTIFIC_VALUE = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "scientificValue");
    private static final Property NAME = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "name");

    private final Resource subject;

    public PublisherGenerator(UUID identifier) {
        this.model = ModelFactory.createDefaultModel();
        this.subject =
            model.createResource(Constants.publisherUri(identifier));
        model.add(subject, TYPE, Publisher);
        model.add(subject, IDENTIFIER, model.createLiteral(identifier.toString()));
    }

    @Override
    public Model build() {
        return model;
    }

    @Override
    public PublicationContext withOnlineIssn(String onlineIssn) {
        model.add(subject, ONLINE_ISSN, model.createLiteral(onlineIssn));
        return this;
    }

    @Override
    public PublicationContext withPrintIssn(String printIssn) {
        model.add(subject, PRINT_ISSN, model.createLiteral(printIssn));
        return null;
    }

    @Override
    public Resource getSubject() {
        return subject;
    }

    @Override
    public PublisherGenerator withScientificValue(String value) {
        model.add(subject, SCIENTIFIC_VALUE, model.createLiteral(value));
        return this;
    }

    @Override
    public PublicationContext withName(String name) {
        model.add(subject, NAME, model.createLiteral(name));
        return this;
    }
}

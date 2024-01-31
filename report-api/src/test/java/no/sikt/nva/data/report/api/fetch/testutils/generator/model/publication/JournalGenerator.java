package no.sikt.nva.data.report.api.fetch.testutils.generator.model.publication;

import java.util.UUID;
import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class JournalGenerator extends TripleBasedBuilder implements PublicationContext {

    public final Model model;
    private static final Resource JOURNAL = new ResourceImpl(Constants.ONTOLOGY_BASE_URI + "Journal");
    private static final Property SCIENTIFIC_VALUE = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "scientificValue");
    private static final Property NAME = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "name");

    private final Resource subject;

    public JournalGenerator(UUID identifier) {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource(Constants.journalUri(identifier));
        model.add(subject, TYPE, JOURNAL);
    }

    @Override
    public JournalGenerator withOnlineIssn(String issn) {
        model.add(subject, ONLINE_ISSN, model.createLiteral(issn));
        return this;
    }

    public JournalGenerator withPrintIssn(String issn) {
        model.add(subject, PRINT_ISSN, model.createLiteral(issn));
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

    @Override
    public JournalGenerator withScientificValue(String value) {
        model.add(subject, SCIENTIFIC_VALUE, model.createLiteral(value));
        return this;
    }

    @Override
    public PublicationContext withName(String name) {
        model.add(subject, NAME, model.createLiteral(name));
        return this;
    }
}

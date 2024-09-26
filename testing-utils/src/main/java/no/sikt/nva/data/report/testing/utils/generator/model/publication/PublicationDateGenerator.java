package no.sikt.nva.data.report.testing.utils.generator.model.publication;

import no.sikt.nva.data.report.testing.utils.generator.Constants;
import no.sikt.nva.data.report.testing.utils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class PublicationDateGenerator extends TripleBasedBuilder {

    private final Model model;
    private static final Resource PUBLICATION_DATE = new ResourceImpl(Constants.ONTOLOGY_BASE_URI + "PublicationDate");
    private static final Property YEAR = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "year");
    private static final Property MONTH = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "month");
    private static final Property DAY = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "day");

    private final Resource subject;

    public PublicationDateGenerator() {
        super();
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource();
        model.add(subject, TYPE, PUBLICATION_DATE);
    }

    public PublicationDateGenerator withYear(String year) {
        model.add(subject, YEAR, year);
        return this;
    }

    public PublicationDateGenerator withMonth(String month) {
        model.add(subject, MONTH, month);
        return this;
    }

    public PublicationDateGenerator withDay(String day) {
        model.add(subject, DAY, day);
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

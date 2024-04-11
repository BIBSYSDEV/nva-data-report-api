package no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi;

import java.util.UUID;
import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class InstitutionPointsGenerator extends TripleBasedBuilder {

    public static final PropertyImpl BREAKDOWN = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "breakdown");
    private static final Property INSTITUTION_POINTS = new PropertyImpl(Constants.ONTOLOGY_BASE_URI,
                                                                        "InstitutionPoints");
    private final Model model;
    private final Resource subject;

    public InstitutionPointsGenerator() {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource("someBlankNode" + UUID.randomUUID());
        model.add(subject, TYPE, INSTITUTION_POINTS);
    }

    public InstitutionPointsGenerator withPoints(String points) {
        model.add(subject, new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "points"), points);
        return this;
    }

    public InstitutionPointsGenerator withCreatorAffiliationPoints(
        CreatorAffiliationPointsGenerator creatorAffiliationPointsGenerator) {
        model.add(subject, BREAKDOWN, creatorAffiliationPointsGenerator.getSubject());
        model.add(creatorAffiliationPointsGenerator.build());
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

package no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi;

import java.math.BigDecimal;
import java.util.UUID;
import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import org.apache.jena.datatypes.xsd.impl.XSDDouble;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class CreatorAffiliationPointsGenerator extends TripleBasedBuilder {

    public static final PropertyImpl CREATOR_ID = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "nviCreator");
    public static final PropertyImpl AFFILIATION_ID = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "affiliationId");
    public static final PropertyImpl POINTS = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "points");
    private static final Property CREATOR_AFFILIATION_POINTS = new PropertyImpl(Constants.ONTOLOGY_BASE_URI,
                                                                                "CreatorAffiliationPoints");
    private final Model model;
    private final Resource subject;

    public CreatorAffiliationPointsGenerator() {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource("someBlankNode" + UUID.randomUUID());
        model.add(subject, TYPE, CREATOR_AFFILIATION_POINTS);
    }

    public CreatorAffiliationPointsGenerator withCreatorId(String creatorId) {
        model.add(subject, CREATOR_ID, creatorId);
        return this;
    }

    public CreatorAffiliationPointsGenerator withAffiliationId(String affiliationId) {
        model.add(subject, AFFILIATION_ID, affiliationId);
        return this;
    }

    public CreatorAffiliationPointsGenerator withPoints(BigDecimal points) {
        model.add(subject, POINTS, model.createTypedLiteral(points, XSDDouble.XSDdouble));
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

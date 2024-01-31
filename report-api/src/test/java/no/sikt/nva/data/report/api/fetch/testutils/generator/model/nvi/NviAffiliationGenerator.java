package no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi;

import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class NviAffiliationGenerator extends TripleBasedBuilder {

    private static final Property IS_NVI_AFFILIATION = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "isNviAffiliation");

    private final Model model;
    private final Resource subject;

    public NviAffiliationGenerator(String id) {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource(id);
        model.add(subject, TYPE, model.createResource(Constants.ONTOLOGY_BASE_URI + "Organization"));
        model.add(subject, IS_NVI_AFFILIATION, model.createTypedLiteral(true));
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

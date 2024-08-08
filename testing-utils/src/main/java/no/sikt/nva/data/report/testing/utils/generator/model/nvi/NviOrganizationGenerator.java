package no.sikt.nva.data.report.testing.utils.generator.model.nvi;

import java.util.List;
import no.sikt.nva.data.report.testing.utils.generator.Constants;
import no.sikt.nva.data.report.testing.utils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class NviOrganizationGenerator extends TripleBasedBuilder {

    private static final Property PART_OF = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "partOf");
    private static final String NVI_ORGANIZATION = "NviOrganization";

    private final Model model;
    private final Resource subject;

    public NviOrganizationGenerator(String id) {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource(id);
        model.add(subject, TYPE, model.createResource(Constants.ONTOLOGY_BASE_URI + NVI_ORGANIZATION));
    }

    public NviOrganizationGenerator withPartOf(List<String> partOfList) {
        partOfList.forEach(partOf -> model.add(subject, PART_OF, model.createResource(partOf)));
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

package no.sikt.nva.data.report.api.fetch.testutils.generator.model.publication;

import static java.util.Objects.nonNull;
import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class OrganizationGenerator extends TripleBasedBuilder {

    public final Model model;
    private static final Property LABEL = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "label");
    private static final Property HAS_PART = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "hasPart") {
    };
    private static final Property PART_OF = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "partOf");

    public final Resource subject;

    public OrganizationGenerator(String id) {
        model = ModelFactory.createDefaultModel();
        this.subject = model.createResource(id);
        model.add(subject, TYPE, model.createResource(Constants.ONTOLOGY_BASE_URI + "Organization"));
    }

    public OrganizationGenerator withLabel(String label, String language) {
        if (nonNull(label)) {
            model.add(subject, LABEL, model.createLiteral(label, language));
        }
        return this;
    }

    public OrganizationGenerator withPartOf(OrganizationGenerator organization) {
        if (nonNull(organization)) {
            model.add(subject, PART_OF, organization.getSubject());
            model.add(organization.getSubject(), HAS_PART, subject);
            model.add(organization.model);
        }
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

package no.sikt.nva.data.report.api.fetch.testutils.generator.model;

import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class RoleGenerator extends TripleBasedBuilder {

    public final Model model;
    public final Resource subject;

    public RoleGenerator(String role) {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource();
        model.add(subject, TYPE, model.createResource(Constants.ONTOLOGY_BASE_URI + role));
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

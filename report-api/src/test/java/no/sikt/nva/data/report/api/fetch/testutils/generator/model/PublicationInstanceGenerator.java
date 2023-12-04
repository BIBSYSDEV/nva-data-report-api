package no.sikt.nva.data.report.api.fetch.testutils.generator.model;

import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class PublicationInstanceGenerator extends TripleBasedBuilder {

    private final Model model;

    private final Resource subject;

    public PublicationInstanceGenerator(String typeName) {
        this.model =  ModelFactory.createDefaultModel();
        this.subject = model.createResource();
        var instanceType = model.createResource(Constants.ONTOLOGY_BASE_URI + typeName);
        model.add(subject, TYPE, instanceType);
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

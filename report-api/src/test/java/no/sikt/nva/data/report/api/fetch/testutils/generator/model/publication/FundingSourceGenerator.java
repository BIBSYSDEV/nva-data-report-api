package no.sikt.nva.data.report.api.fetch.testutils.generator.model.publication;

import static no.sikt.nva.data.report.api.fetch.testutils.generator.model.publication.PublicationGenerator.IDENTIFIER;
import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class FundingSourceGenerator extends TripleBasedBuilder {

    public final Model model;
    private static final Resource FUNDING_SOURCE = new ResourceImpl(Constants.ONTOLOGY_BASE_URI + "FundingSource");
    public final Resource subject;

    public FundingSourceGenerator() {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource(Constants.fundingSourceUri("NFR"));
        model.add(subject, TYPE, FUNDING_SOURCE);
    }

    public FundingSourceGenerator withIdentifier(String identifier) {
        model.add(subject, IDENTIFIER, model.createLiteral(identifier));
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

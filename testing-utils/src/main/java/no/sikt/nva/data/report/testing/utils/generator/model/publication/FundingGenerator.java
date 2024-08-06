package no.sikt.nva.data.report.testing.utils.generator.model.publication;

import static java.util.Objects.nonNull;
import java.util.concurrent.ThreadLocalRandom;
import no.sikt.nva.data.report.testing.utils.generator.Constants;
import no.sikt.nva.data.report.testing.utils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class FundingGenerator extends TripleBasedBuilder {

    public final Model model;
    private static final int MIN = 10_000;
    private static final int MAX = 60_000;
    private static final Resource FUNDING = new ResourceImpl(Constants.ONTOLOGY_BASE_URI + "Funding");
    private static final Property SOURCE = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "source");

    public final Resource subject;

    public FundingGenerator() {
        this.model = ModelFactory.createDefaultModel();
        var number = ThreadLocalRandom.current().nextInt(MIN, MAX);
        this.subject = model.createResource(Constants.verifiedFundingUri(number));
        model.add(subject, TYPE, FUNDING);
    }

    public FundingGenerator withSource(FundingSourceGenerator source) {
        model.add(subject, SOURCE, source.getSubject());
        model.add(source.build());
        return this;
    }

    public FundingGenerator withIdentifier(String identifier) {
        if (nonNull(identifier)) {
            model.add(subject, PublicationGenerator.IDENTIFIER, model.createLiteral(identifier));
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

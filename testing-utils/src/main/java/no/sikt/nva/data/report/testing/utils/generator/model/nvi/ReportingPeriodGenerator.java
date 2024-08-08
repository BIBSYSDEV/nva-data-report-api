package no.sikt.nva.data.report.testing.utils.generator.model.nvi;

import static java.util.Objects.nonNull;
import no.sikt.nva.data.report.testing.utils.generator.Constants;
import no.sikt.nva.data.report.testing.utils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class ReportingPeriodGenerator extends TripleBasedBuilder {

    private static final Property YEAR = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "year");
    private static final String REPORTING_PERIOD = "ReportingPeriod";

    private final Model model;
    private final Resource subject;

    public ReportingPeriodGenerator() {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource();
        model.add(subject, TYPE, model.createResource(Constants.ONTOLOGY_BASE_URI + REPORTING_PERIOD));
    }

    public ReportingPeriodGenerator withYear(String year) {
        if (nonNull(year)) {
            model.add(subject, YEAR, model.createTypedLiteral(year));
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

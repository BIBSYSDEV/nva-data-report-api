package no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi;

import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class ApprovalGenerator extends TripleBasedBuilder {

    private static final Property APPROVAL = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "Approval");
    private final Model model;
    private final Resource subject;

    public ApprovalGenerator() {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource("someBlankNode");
        model.add(subject, TYPE, APPROVAL);
    }

    public ApprovalGenerator withInstitutionId(String institutionId) {
        model.add(subject, new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "institutionId"), institutionId);
        return this;
    }

    public ApprovalGenerator withApprovalStatus(String approvalStatus) {
        model.add(subject, new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "approvalStatus"), approvalStatus);
        return this;
    }

    public ApprovalGenerator withPoints(String points) {
        model.add(subject, new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "points"), points);
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

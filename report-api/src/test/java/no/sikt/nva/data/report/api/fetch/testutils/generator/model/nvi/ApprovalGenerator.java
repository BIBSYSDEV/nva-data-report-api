package no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi;

import java.util.UUID;
import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.publication.OrganizationGenerator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class ApprovalGenerator extends TripleBasedBuilder {

    private static final Property APPROVAL = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "Approval");
    private static final String INSTITUTION_ID = "institutionId";
    private static final String APPROVAL_STATUS = "approvalStatus";
    private static final String POINTS = "points";
    private final Model model;
    private final Resource subject;

    public ApprovalGenerator() {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource("someBlankNode" + UUID.randomUUID());
        model.add(subject, TYPE, APPROVAL);
    }

    public ApprovalGenerator withInstitutionId(OrganizationGenerator organizationGenerator) {
        model.add(subject, new PropertyImpl(Constants.ONTOLOGY_BASE_URI, INSTITUTION_ID),
                  organizationGenerator.subject);
        return this;
    }

    public ApprovalGenerator withApprovalStatus(String approvalStatus) {
        model.add(subject, new PropertyImpl(Constants.ONTOLOGY_BASE_URI, APPROVAL_STATUS), approvalStatus);
        return this;
    }

    public ApprovalGenerator withPoints(String points) {
        model.add(subject, new PropertyImpl(Constants.ONTOLOGY_BASE_URI, POINTS), points);
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

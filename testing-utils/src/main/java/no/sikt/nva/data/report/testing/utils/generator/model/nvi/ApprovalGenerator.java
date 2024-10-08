package no.sikt.nva.data.report.testing.utils.generator.model.nvi;

import java.util.Set;
import no.sikt.nva.data.report.testing.utils.generator.Constants;
import no.sikt.nva.data.report.testing.utils.generator.model.TripleBasedBuilder;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.OrganizationGenerator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public class ApprovalGenerator extends TripleBasedBuilder {

    public static final PropertyImpl POINTS_PROPERTY = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "points");
    public static final PropertyImpl APPROVAL_STATUS = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "approvalStatus");
    public static final PropertyImpl INSTITUTION_ID = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "institutionId");
    public static final Property INVOLVED_ORGANIZATION = new PropertyImpl(Constants.ONTOLOGY_BASE_URI,
                                                                          "involvedOrganization");
    private static final Property APPROVAL = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "Approval");
    private final Model model;
    private final Resource subject;

    public ApprovalGenerator() {
        super();
        this.model = ModelFactory.createDefaultModel();
        this.subject = BlankNodeUtil.createRandom(model);
        model.add(subject, TYPE, APPROVAL);
    }

    public ApprovalGenerator withInstitutionId(OrganizationGenerator organizationGenerator) {
        model.add(subject, INSTITUTION_ID, organizationGenerator.getSubject());
        return this;
    }

    public ApprovalGenerator withApprovalStatus(String approvalStatus) {
        model.add(subject, APPROVAL_STATUS, approvalStatus);
        return this;
    }

    public ApprovalGenerator withPoints(InstitutionPointsGenerator institutionPointsGenerator) {
        model.add(subject, POINTS_PROPERTY, institutionPointsGenerator.getSubject());
        model.add(institutionPointsGenerator.build());
        return this;
    }

    public ApprovalGenerator withInvolvedOrganizations(Set<String> involvedOrganizations) {
        involvedOrganizations.forEach(organization -> model.add(subject, INVOLVED_ORGANIZATION, organization));
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
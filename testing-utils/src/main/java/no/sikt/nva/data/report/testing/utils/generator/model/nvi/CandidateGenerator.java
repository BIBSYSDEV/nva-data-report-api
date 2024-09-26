package no.sikt.nva.data.report.testing.utils.generator.model.nvi;

import java.math.BigDecimal;
import no.sikt.nva.data.report.testing.utils.generator.Constants;
import no.sikt.nva.data.report.testing.utils.generator.model.TripleBasedBuilder;
import org.apache.jena.datatypes.xsd.impl.XSDDateTimeType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class CandidateGenerator extends TripleBasedBuilder {

    private static final Resource NVI_CANDIDATE = new ResourceImpl(Constants.ONTOLOGY_BASE_URI + "NviCandidate");
    private static final Property PUBLICATION_DETAILS = new PropertyImpl(
        Constants.ONTOLOGY_BASE_URI + "publicationDetails");
    private static final Property MODIFIED_DATE = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "modifiedDate");
    private static final Property IDENTIFIER = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "identifier");
    private static final Property APPROVAL = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "approval");
    private static final Property IS_APPLICABLE = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "isApplicable");
    private static final Property POINTS = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "points");
    private static final Property PUBLICATION_TYPE_CHANNEL_LEVEL_POINTS = new PropertyImpl(
        Constants.ONTOLOGY_BASE_URI + "publicationTypeChannelLevelPoints");
    private static final Property CREATOR_SHARE_COUNT = new PropertyImpl(
        Constants.ONTOLOGY_BASE_URI + "creatorShareCount");
    private static final Property INTERNATIONAL_COLLABORATION_FACTOR = new PropertyImpl(
        Constants.ONTOLOGY_BASE_URI + "internationalCollaborationFactor");
    private static final Property REPORTED = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "reported");
    private static final Property REPORTING_PERIOD = new PropertyImpl(Constants.ONTOLOGY_BASE_URI + "reportingPeriod");
    private static final Property GLOBAL_APPROVAL_STATUS = new PropertyImpl(
        Constants.ONTOLOGY_BASE_URI + "globalApprovalStatus");
    private final Model model;
    private final Resource subject;

    public CandidateGenerator(String id, String identifier, String modifiedDate) {
        super();
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource(id);
        model.add(subject, MODIFIED_DATE, model.createTypedLiteral(modifiedDate, XSDDateTimeType.XSDdateTime));
        model.add(subject, IDENTIFIER, model.createLiteral(identifier));
        model.add(subject, TYPE, NVI_CANDIDATE);
    }

    public CandidateGenerator withPublicationDetails(PublicationDetailsGenerator publicationDetails) {
        model.add(subject, PUBLICATION_DETAILS, publicationDetails.getSubject());
        model.add(publicationDetails.build());
        return this;
    }

    public CandidateGenerator withApproval(ApprovalGenerator approval) {
        model.add(subject, APPROVAL, approval.getSubject());
        model.add(approval.build());
        return this;
    }

    public CandidateGenerator withReportingPeriod(ReportingPeriodGenerator reportingPeriod) {
        model.add(subject, REPORTING_PERIOD, reportingPeriod.getSubject());
        model.add(reportingPeriod.build());
        return this;
    }

    public CandidateGenerator withIsApplicable(boolean isApplicable) {
        model.add(subject, IS_APPLICABLE, model.createTypedLiteral(isApplicable));
        return this;
    }

    public CandidateGenerator withPoints(String points) {
        model.add(subject, POINTS, model.createTypedLiteral(points));
        return this;
    }

    public CandidateGenerator withPublicationTypeChannelLevelPoints(BigDecimal publicationTypeChannelLevelPoints) {
        model.add(subject, PUBLICATION_TYPE_CHANNEL_LEVEL_POINTS,
                  model.createTypedLiteral(publicationTypeChannelLevelPoints));
        return this;
    }

    public CandidateGenerator withCreatorShareCount(String creatorShareCount) {
        model.add(subject, CREATOR_SHARE_COUNT, model.createTypedLiteral(creatorShareCount));
        return this;
    }

    public CandidateGenerator withInternationalCollaborationFactor(BigDecimal internationalCollaborationFactor) {
        model.add(subject, INTERNATIONAL_COLLABORATION_FACTOR,
                  model.createTypedLiteral(internationalCollaborationFactor));
        return this;
    }

    public CandidateGenerator withReported(boolean reported) {
        model.add(subject, REPORTED, model.createTypedLiteral(reported));
        return this;
    }

    public CandidateGenerator withGlobalApprovalStatus(String globalApprovalStatus) {
        model.add(subject, GLOBAL_APPROVAL_STATUS, model.createTypedLiteral(globalApprovalStatus));
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

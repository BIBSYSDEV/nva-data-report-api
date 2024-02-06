package no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi;

import static no.sikt.nva.data.report.api.fetch.testutils.generator.Constants.ONTOLOGY_BASE_URI;
import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.TripleBasedBuilder;
import org.apache.jena.datatypes.xsd.impl.XSDDateTimeType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class CandidateGenerator extends TripleBasedBuilder {

    private static final Resource NVI_CANDIDATE = new ResourceImpl(ONTOLOGY_BASE_URI + "NviCandidate");
    private static final Property PUBLICATION_DETAILS = new PropertyImpl(ONTOLOGY_BASE_URI + "publicationDetails");
    private static final Property MODIFIED_DATE = new PropertyImpl(ONTOLOGY_BASE_URI + "modifiedDate");
    private static final Property IDENTIFIER = new PropertyImpl(ONTOLOGY_BASE_URI + "identifier");
    private static final Property APPROVAL = new PropertyImpl(ONTOLOGY_BASE_URI + "approval");
    private final Model model;
    private final Resource subject;

    public CandidateGenerator(String identifier, String modifiedDate) {
        this.model = ModelFactory.createDefaultModel();
        this.subject = model.createResource(Constants.NVI_CANDIDATE_BASE_URI + identifier);
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

    @Override
    public Model build() {
        return model;
    }

    @Override
    public Resource getSubject() {
        return subject;
    }
}

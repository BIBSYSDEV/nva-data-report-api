package no.sikt.nva.data.report.testing.utils.model;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.List;
import java.util.Set;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleApproval;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleCreatorAffiliationPoints;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleInstitutionPoints;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleNviCandidate;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleNviContributor;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleNviOrganization;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SamplePublicationDetails;
import no.unit.nva.commons.json.JsonSerializable;

public record NviIndexDocument(@JsonProperty("@context") String context,
                               String id,
                               boolean isApplicable,
                               String type,
                               String identifier,
                               PublicationDetails publicationDetails,
                               List<Approval> approvals,
                               double points,
                               double publicationTypeChannelLevelPoints,
                               String globalApprovalStatus,
                               int creatorShareCount,
                               double internationalCollaborationFactor,
                               ReportingPeriod reportingPeriod,
                               boolean reported,
                               String modifiedDate) implements JsonSerializable {

    public static final String CONTEXT = "https://api.dev.nva.aws.unit.no/scientific-index/context";
    public static final String TYPE = "NviCandidate";

    public static NviIndexDocument from(SampleNviCandidate nviCandidate) {
        return new NviIndexDocument(CONTEXT,
                                    nviCandidate.candidateUri(),
                                    nviCandidate.isApplicable(),
                                    TYPE,
                                    nviCandidate.identifier(),
                                    PublicationDetails.from(nviCandidate.publicationDetails()),
                                    generateApprovals(nviCandidate),
                                    nviCandidate.totalPoints().doubleValue(),
                                    nviCandidate.publicationTypeChannelLevelPoints().doubleValue(),
                                    nviCandidate.globalApprovalStatus().getValue(),
                                    nviCandidate.creatorShareCount(),
                                    nviCandidate.internationalCollaborationFactor().doubleValue(),
                                    ReportingPeriod.from(nviCandidate.reportingPeriod()),
                                    nviCandidate.reported(),
                                    nviCandidate.modifiedDate().toString());
    }

    public JsonNode asJsonNode() {
        return attempt(() -> dtoObjectMapper.readTree(this.toJsonString())).orElseThrow();
    }

    public IndexDocument toIndexDocument() {
        return IndexDocument.from(this);
    }

    private static List<Approval> generateApprovals(SampleNviCandidate nviCandidate) {
        return nviCandidate.approvals()
                   .stream()
                   .map(testApproval -> Approval.from(testApproval, nviCandidate.globalApprovalStatus().getValue()))
                   .toList();
    }

    private record ReportingPeriod(String type, String year) {

        public static final String TYPE = "ReportingPeriod";

        public static ReportingPeriod from(String year) {
            return new ReportingPeriod(TYPE, year);
        }
    }

    private record Approval(String type,
                            URI institutionId,
                            String approvalStatus,
                            Points points,
                            Set<String> involvedOrganizations,
                            String globalApprovalStatus) {

        public static final String TYPE = "Approval";

        public static Approval from(SampleApproval sampleApproval, String globalApprovalStatus) {
            return new Approval(TYPE,
                                sampleApproval.institutionId(),
                                sampleApproval.approvalStatus().getValue(),
                                Points.from(sampleApproval.points()),
                                sampleApproval.involvedOrganizations(),
                                globalApprovalStatus);
        }

        private record Points(String type,
                              double institutionPoints,
                              List<CreatorAffiliationPoints> creatorAffiliationPoints) {

            public static final String TYPE = "InstitutionPoints";

            public static Points from(SampleInstitutionPoints points) {
                return new Points(TYPE,
                                  points.institutionPoints().doubleValue(),
                                  points.creatorAffiliationPoints()
                                      .stream()
                                      .map(CreatorAffiliationPoints::from)
                                      .toList());
            }

            private record CreatorAffiliationPoints(String type,
                                                    URI nviCreator,
                                                    URI affiliationId,
                                                    double points) {

                public static final String TYPE = "CreatorAffiliationPoints";

                public static CreatorAffiliationPoints from(
                    SampleCreatorAffiliationPoints sampleCreatorAffiliationPoints) {
                    return new CreatorAffiliationPoints(TYPE,
                                                        sampleCreatorAffiliationPoints.nviCreator(),
                                                        sampleCreatorAffiliationPoints.affiliationId(),
                                                        sampleCreatorAffiliationPoints.points().doubleValue());
                }
            }
        }
    }

    private record PublicationDetails(String id,
                                      List<NviContributor> contributors) {

        public static PublicationDetails from(SamplePublicationDetails samplePublicationDetails) {
            return new PublicationDetails(samplePublicationDetails.id(),
                                          samplePublicationDetails.contributors().stream()
                                              .map(NviContributor::from)
                                              .toList());
        }

        private record NviContributor(String type,
                                      String id,
                                      List<NviOrganization> affiliations) {

            public static final String TYPE = "NviContributor";

            public static NviContributor from(SampleNviContributor sampleNviContributor) {
                return new NviContributor(TYPE,
                                          sampleNviContributor.id(),
                                          sampleNviContributor.affiliations()
                                              .stream()
                                              .map(NviOrganization::from)
                                              .toList());
            }

            private record NviOrganization(String type,
                                           String id,
                                           List<String> partOf) {

                public static final String TYPE = "NviOrganization";

                public static NviOrganization from(SampleNviOrganization sampleNviOrganization) {
                    return new NviOrganization(TYPE,
                                               sampleNviOrganization.id(),
                                               sampleNviOrganization.partOf());
                }
            }
        }
    }
}

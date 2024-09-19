package no.sikt.nva.data.report.testing.utils.model;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.List;
import java.util.Set;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestApproval;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestCreatorAffiliationPoints;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestInstitutionPoints;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviCandidate;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviContributor;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviOrganization;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestPublicationDetails;
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

    public static NviIndexDocument from(TestNviCandidate nviCandidate) {
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

    private static List<Approval> generateApprovals(TestNviCandidate nviCandidate) {
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

        public static Approval from(TestApproval testApproval, String globalApprovalStatus) {
            return new Approval(TYPE,
                                testApproval.institutionId(),
                                testApproval.approvalStatus().getValue(),
                                Points.from(testApproval.points()),
                                testApproval.involvedOrganizations(),
                                globalApprovalStatus);
        }

        private record Points(String type,
                              double institutionPoints,
                              List<CreatorAffiliationPoints> creatorAffiliationPoints) {

            public static final String TYPE = "InstitutionPoints";

            public static Points from(TestInstitutionPoints points) {
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

                public static CreatorAffiliationPoints from(TestCreatorAffiliationPoints testCreatorAffiliationPoints) {
                    return new CreatorAffiliationPoints(TYPE,
                                                        testCreatorAffiliationPoints.nviCreator(),
                                                        testCreatorAffiliationPoints.affiliationId(),
                                                        testCreatorAffiliationPoints.points().doubleValue());
                }
            }
        }
    }

    private record PublicationDetails(String id,
                                      List<NviContributor> contributors) {

        public static PublicationDetails from(TestPublicationDetails testPublicationDetails) {
            return new PublicationDetails(testPublicationDetails.id(),
                                          testPublicationDetails.contributors().stream()
                                              .map(NviContributor::from)
                                              .toList());
        }

        private record NviContributor(String type,
                                      String id,
                                      List<NviOrganization> affiliations) {

            public static final String TYPE = "NviContributor";

            public static NviContributor from(TestNviContributor testNviContributor) {
                return new NviContributor(TYPE,
                                          testNviContributor.id(),
                                          testNviContributor.affiliations()
                                              .stream()
                                              .map(NviOrganization::from)
                                              .toList());
            }

            private record NviOrganization(String type,
                                           String id,
                                           List<String> partOf) {

                public static final String TYPE = "NviOrganization";

                public static NviOrganization from(TestNviOrganization testNviOrganization) {
                    return new NviOrganization(TYPE,
                                               testNviOrganization.id(),
                                               testNviOrganization.partOf());
                }
            }
        }
    }
}

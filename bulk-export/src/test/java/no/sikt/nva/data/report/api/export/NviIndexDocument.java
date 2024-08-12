package no.sikt.nva.data.report.api.export;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestApproval;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviCandidate;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviContributor;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviOrganization;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestPublicationDetails;

//{
//    "id" : "https://api.dev.nva.aws.unit.no/scientific-index/candidate/000012a8-7ebb-4d56-b02b-d5b0124eb70c",
//    "isApplicable" : true,
//    "type" : "NviCandidate",
//    "identifier" : "000012a8-7ebb-4d56-b02b-d5b0124eb70c",
//    "publicationDetails" : {
//    "id" : "https://api.dev.nva.aws.unit.no/publication/019054d844c7-074e42ba-cf75-4bf8-8935-313b7f4d78b5",
//    "type" : "AcademicArticle",
//    "title" : "Comparison of sleep latency and number of SOREMPs in the home and hospital with a modified multiple
//    sleep latency test: A randomized crossover study",
//    "publicationDate" : {
//    "year" : "2017"
//    },
//    "contributors" : [ {
//    "type" : "NviContributor",
//    "id" : "https://api.dev.nva.aws.unit.no/cristin/person/398939",
//    "name" : "Kornelia Katalin Beiske",
//    "role" : "Creator",
//    "affiliations" : [ {
//    "type" : "NviOrganization",
//    "id" : "https://api.dev.nva.aws.unit.no/cristin/organization/1972.40.8.0",
//    "identifier" : "1972.40.8.0",
//    "partOf" : [ "https://api.dev.nva.aws.unit.no/cristin/organization/1972.40.0.0", "https://api.dev.nva.aws.unit
//    .no/cristin/organization/1972.0.0.0" ],
//    "partOfIdentifiers" : [ "1972.40.0.0", "1972.0.0.0" ]
//    } ]
//    }, {
//    "type" : "NviContributor",
//    "id" : "https://api.dev.nva.aws.unit.no/cristin/person/43500",
//    "name" : "Trond Sand",
//    "role" : "Creator",
//    "affiliations" : [ {
//    "type" : "NviOrganization",
//    "id" : "https://api.dev.nva.aws.unit.no/cristin/organization/1920.16.0.0",
//    "identifier" : "1920.16.0.0",
//    "partOf" : [ "https://api.dev.nva.aws.unit.no/cristin/organization/1920.16.0.0", "https://api.dev.nva.aws.unit
//    .no/cristin/organization/1920.0.0.0" ],
//    "partOfIdentifiers" : [ "1920.16.0.0", "1920.0.0.0" ]
//    }, {
//    "type" : "NviOrganization",
//    "id" : "https://api.dev.nva.aws.unit.no/cristin/organization/194.65.30.0",
//    "identifier" : "194.65.30.0",
//    "partOf" : [ "https://api.dev.nva.aws.unit.no/cristin/organization/194.65.0.0", "https://api.dev.nva.aws.unit
//    .no/cristin/organization/194.0.0.0" ],
//    "partOfIdentifiers" : [ "194.65.0.0", "194.0.0.0" ]
//    } ]
//    }, {
//    "type" : "NviContributor",
//    "id" : "https://api.dev.nva.aws.unit.no/cristin/person/405669",
//    "name" : "Eyvind Rugland",
//    "role" : "Creator",
//    "affiliations" : [ {
//    "type" : "NviOrganization",
//    "id" : "https://api.dev.nva.aws.unit.no/cristin/organization/1972.40.8.0",
//    "identifier" : "1972.40.8.0",
//    "partOf" : [ "https://api.dev.nva.aws.unit.no/cristin/organization/1972.40.0.0", "https://api.dev.nva.aws.unit
//    .no/cristin/organization/1972.0.0.0" ],
//    "partOfIdentifiers" : [ "1972.40.0.0", "1972.0.0.0" ]
//    } ]
//    }, {
//    "type" : "NviContributor",
//    "id" : "https://api.dev.nva.aws.unit.no/cristin/person/11988",
//    "name" : "Knut Stavem",
//    "role" : "Creator",
//    "affiliations" : [ {
//    "type" : "NviOrganization",
//    "id" : "https://api.dev.nva.aws.unit.no/cristin/organization/1972.90.0.1",
//    "identifier" : "1972.90.0.1",
//    "partOf" : [ "https://api.dev.nva.aws.unit.no/cristin/organization/1972.90.0.0", "https://api.dev.nva.aws.unit
//    .no/cristin/organization/1972.0.0.0" ],
//    "partOfIdentifiers" : [ "1972.90.0.0", "1972.0.0.0" ]
//    }, {
//    "type" : "NviOrganization",
//    "id" : "https://api.dev.nva.aws.unit.no/cristin/organization/1972.40.5.0",
//    "identifier" : "1972.40.5.0",
//    "partOf" : [ "https://api.dev.nva.aws.unit.no/cristin/organization/1972.40.0.0", "https://api.dev.nva.aws.unit
//    .no/cristin/organization/1972.0.0.0" ],
//    "partOfIdentifiers" : [ "1972.40.0.0", "1972.0.0.0" ]
//    }, {
//    "type" : "NviOrganization",
//    "id" : "https://api.dev.nva.aws.unit.no/cristin/organization/185.53.82.0",
//    "identifier" : "185.53.82.0",
//    "partOf" : [ "https://api.dev.nva.aws.unit.no/cristin/organization/185.53.80.0", "https://api.dev.nva.aws.unit
//    .no/cristin/organization/185.90.0.0", "https://api.dev.nva.aws.unit.no/cristin/organization/185.53.0.0",
//    "https://api.dev.nva.aws.unit.no/cristin/organization/185.50.0.0" ],
//    "partOfIdentifiers" : [ "185.53.80.0", "185.90.0.0", "185.53.0.0", "185.50.0.0" ]
//    } ]
//    } ]
//    },
//    "approvals" : [ {
//    "type" : "Approval",
//    "institutionId" : "https://api.dev.nva.aws.unit.no/cristin/organization/1920.0.0.0",
//    "labels" : {
//    "nb" : "St. Olavs Hospital HF",
//    "en" : "St. Olavs Hospital, Trondheim University Hospital"
//    },
//    "approvalStatus" : "Approved",
//    "points" : {
//    "type" : "InstitutionPoints",
//    "institutionId" : "https://api.dev.nva.aws.unit.no/cristin/organization/1920.0.0.0",
//    "institutionPoints" : 0.4082482905,
//    "creatorAffiliationPoints" : [ {
//    "type" : "CreatorAffiliationPoints",
//    "nviCreator" : "https://api.dev.nva.aws.unit.no/cristin/person/43500",
//    "affiliationId" : "https://api.dev.nva.aws.unit.no/cristin/organization/1920.16.0.0",
//    "points" : 0.4082482905
//    } ]
//    },
//    "involvedOrganizations" : [ "https://api.dev.nva.aws.unit.no/cristin/organization/1920.0.0.0", "https://api.dev
//    .nva.aws.unit.no/cristin/organization/1920.16.0.0" ],
//    "globalApprovalStatus" : "Approved"
//    }, {
//    "type" : "Approval",
//    "institutionId" : "https://api.dev.nva.aws.unit.no/cristin/organization/194.0.0.0",
//    "labels" : {
//    "nn" : "Noregs teknisk-naturvitskaplege universitet",
//    "nb" : "Norges teknisk-naturvitenskapelige universitet",
//    "en" : "Norwegian University of Science and Technology"
//    },
//    "approvalStatus" : "Approved",
//    "points" : {
//    "type" : "InstitutionPoints",
//    "institutionId" : "https://api.dev.nva.aws.unit.no/cristin/organization/194.0.0.0",
//    "institutionPoints" : 0.4082482905,
//    "creatorAffiliationPoints" : [ {
//    "type" : "CreatorAffiliationPoints",
//    "nviCreator" : "https://api.dev.nva.aws.unit.no/cristin/person/43500",
//    "affiliationId" : "https://api.dev.nva.aws.unit.no/cristin/organization/194.65.30.0",
//    "points" : 0.4082482905
//    } ]
//    },
//    "involvedOrganizations" : [ "https://api.dev.nva.aws.unit.no/cristin/organization/194.65.30.0", "https://api
//    .dev.nva.aws.unit.no/cristin/organization/194.0.0.0", "https://api.dev.nva.aws.unit.no/cristin/organization/194.65.0.0"
//    ],
//    "globalApprovalStatus" : "Approved"
//    }, {
//    "type" : "Approval",
//    "institutionId" : "https://api.dev.nva.aws.unit.no/cristin/organization/185.90.0.0",
//    "labels" : {
//    "nb" : "Universitetet i Oslo",
//    "en" : "University of Oslo"
//    },
//    "approvalStatus" : "Approved",
//    "points" : {
//    "type" : "InstitutionPoints",
//    "institutionId" : "https://api.dev.nva.aws.unit.no/cristin/organization/185.90.0.0",
//    "institutionPoints" : 0.4082482905,
//    "creatorAffiliationPoints" : [ {
//    "type" : "CreatorAffiliationPoints",
//    "nviCreator" : "https://api.dev.nva.aws.unit.no/cristin/person/11988",
//    "affiliationId" : "https://api.dev.nva.aws.unit.no/cristin/organization/185.53.82.0",
//    "points" : 0.4082482905
//    } ]
//    },
//    "involvedOrganizations" : [ "https://api.dev.nva.aws.unit.no/cristin/organization/185.53.80.0", "https://api
//    .dev.nva.aws.unit.no/cristin/organization/185.50.0.0", "https://api.dev.nva.aws.unit.no/cristin/organization/185.90.0.0",
//    "https://api.dev.nva.aws.unit.no/cristin/organization/185.53.0.0", "https://api.dev.nva.aws.unit
//    .no/cristin/organization/185.53.82.0" ],
//    "globalApprovalStatus" : "Approved"
//    }, {
//    "type" : "Approval",
//    "institutionId" : "https://api.dev.nva.aws.unit.no/cristin/organization/1972.0.0.0",
//    "labels" : {
//    "nb" : "Akershus universitetssykehus HF",
//    "en" : "Akershus University Hospital Trust"
//    },
//    "approvalStatus" : "Approved",
//    "points" : {
//    "type" : "InstitutionPoints",
//    "institutionId" : "https://api.dev.nva.aws.unit.no/cristin/organization/1972.0.0.0",
//    "institutionPoints" : 0.7071067812,
//    "creatorAffiliationPoints" : [ {
//    "type" : "CreatorAffiliationPoints",
//    "nviCreator" : "https://api.dev.nva.aws.unit.no/cristin/person/398939",
//    "affiliationId" : "https://api.dev.nva.aws.unit.no/cristin/organization/1972.40.8.0",
//    "points" : 0.2357022604
//    }, {
//    "type" : "CreatorAffiliationPoints",
//    "nviCreator" : "https://api.dev.nva.aws.unit.no/cristin/person/405669",
//    "affiliationId" : "https://api.dev.nva.aws.unit.no/cristin/organization/1972.40.8.0",
//    "points" : 0.2357022604
//    }, {
//    "type" : "CreatorAffiliationPoints",
//    "nviCreator" : "https://api.dev.nva.aws.unit.no/cristin/person/11988",
//    "affiliationId" : "https://api.dev.nva.aws.unit.no/cristin/organization/1972.40.5.0",
//    "points" : 0.1178511302
//    }, {
//    "type" : "CreatorAffiliationPoints",
//    "nviCreator" : "https://api.dev.nva.aws.unit.no/cristin/person/11988",
//    "affiliationId" : "https://api.dev.nva.aws.unit.no/cristin/organization/1972.90.0.1",
//    "points" : 0.1178511302
//    } ]
//    },
//    "involvedOrganizations" : [ "https://api.dev.nva.aws.unit.no/cristin/organization/1972.40.0.0", "https://api
//    .dev.nva.aws.unit.no/cristin/organization/1972.40.8.0", "https://api.dev.nva.aws.unit.no/cristin/organization/1972.90.0.0",
//    "https://api.dev.nva.aws.unit.no/cristin/organization/1972.90.0.1", "https://api.dev.nva.aws.unit
//    .no/cristin/organization/1972.40.5.0", "https://api.dev.nva.aws.unit.no/cristin/organization/1972.0.0.0" ],
//    "globalApprovalStatus" : "Approved"
//    } ],
//    "numberOfApprovals" : 4,
//    "points" : 1.9318516527,
//    "publicationTypeChannelLevelPoints" : 1,
//    "globalApprovalStatus" : "Approved",
//    "creatorShareCount" : 0,
//    "internationalCollaborationFactor" : 1,
//    "reportingPeriod" : {
//    "type" : "ReportingPeriod",
//    "year" : "2017"
//    },
//    "reported" : true,
//    "createdDate" : "2024-06-27T16:59:26.173837245Z",
//    "modifiedDate" : "2024-06-27T16:59:26.173837245Z",
//    "@context" : "https://api.dev.nva.aws.unit.no/scientific-index/context"
//    }

public record NviIndexDocument(String id,
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
                               String modifiedDate,
                               String context) {

    public static final String CONTEXT = "https://api.dev.nva.aws.unit.no/scientific-index/context";
    public static final String TYPE = "NviCandidate";

    public static NviIndexDocument from(TestNviCandidate nviCandidate) {
        return new NviIndexDocument(nviCandidate.candidateUri(),
                                    nviCandidate.isApplicable(),
                                    TYPE,
                                    nviCandidate.identifier(),
                                    PublicationDetails.from(nviCandidate.publicationDetails()),
                                    nviCandidate.approvals().stream().map(Approval::from).toList(),
                                    nviCandidate.totalPoints().doubleValue(),
                                    nviCandidate.publicationTypeChannelLevelPoints().doubleValue(),
                                    nviCandidate.globalApprovalStatus().getValue(),
                                    nviCandidate.creatorShareCount(),
                                    nviCandidate.internationalCollaborationFactor().doubleValue(),
                                    ReportingPeriod.from(nviCandidate.reportingPeriod()),
                                    nviCandidate.reported(),
                                    nviCandidate.modifiedDate().toString(),
                                    CONTEXT);
    }

    public JsonNode asJsonNode() {
        return null;
    }

    private record ReportingPeriod(String type, String year) {

        public static final String TYPE = "ReportingPeriod";

        public static ReportingPeriod from(String year) {
            return new ReportingPeriod(TYPE, year);
        }
    }

    private record Approval(String type,
                            String institutionId,
                            Map<String, String> labels,
                            String approvalStatus,
                            Points points,
                            List<String> involvedOrganizations,
                            String globalApprovalStatus) {

        public static Approval from(TestApproval testApproval) {
            return null;
        }

        private record Points(String type,
                              String institutionId,
                              double institutionPoints,
                              List<CreatorAffiliationPoints> creatorAffiliationPoints) {

            private record CreatorAffiliationPoints(String type,
                                                    String nviCreator,
                                                    String affiliationId,
                                                    double points) {

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

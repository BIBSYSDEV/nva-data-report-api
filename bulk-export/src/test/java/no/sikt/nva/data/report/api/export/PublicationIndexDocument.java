package no.sikt.nva.data.report.api.export;

import static java.util.Objects.isNull;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestContributor;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestFunding;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestIdentity;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestPublication;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.paths.UriWrapper;

//{
//    "type": "Publication",
//    "publicationContextUris": [
//    "https://api.dev.nva.aws.unit.no/publication-channels-v2/journal/A0314BCC-06E0-447A-B2A5-EDB85B79BBC8/2022"
//    ],
//    "@context": "https://api.dev.nva.aws.unit.no/publication/context",
//    "id": "https://api.dev.nva.aws.unit.no/publication/019055007609-40253705-d66c-47bc-bae1-295f9d10d0ce",
//    "additionalIdentifiers": [
//    {
//    "type": "AdditionalIdentifier",
//    "sourceName": "Cristin",
//    "value": "2095015"
//    },
//    {
//    "type": "AdditionalIdentifier",
//    "sourceName": "handle",
//    "value": "https://hdl.handle.net/11250/3039107"
//    }
//    ],
//    "associatedArtifacts": [
//    {
//    "type": "UnpublishableFile",
//    "administrativeAgreement": true,
//    "identifier": "f3274036-38e4-43f7-b157-aa3bd8375f87",
//    "mimeType": "application/xml",
//    "name": "dublin_core.xml",
//    "rightsRetentionStrategy": {
//    "type": "NullRightsRetentionStrategy",
//    "configuredType": "Unknown"
//    },
//    "size": 6659,
//    "uploadDetails": {
//    "type": "UploadDetails",
//    "uploadedBy": "uis@217.0.0.0",
//    "uploadedDate": "2024-07-01T10:31:11.578074544Z"
//    },
//    "visibleForNonOwner": false
//    },
//    {
//    "type": "PublishedFile",
//    "administrativeAgreement": false,
//    "identifier": "43e85b4e-c737-4db1-9ac5-229c72c1ec32",
//    "license": {
//    "type": "License",
//    "value": "https://creativecommons.org/licenses/by/4.0",
//    "name": "CC-BY",
//    "labels": {
//    "nb": "Creative Commons - Navngivelse",
//    "en": "Creative Commons - Attribution"
//    }
//    },
//    "mimeType": "application/pdf",
//    "name": "%C3%85formes+som+prest+en+psykososial+tiln%C3%A6rming+til+profesjonell+identitet%2C+subjektivitet+og
//    +kj%C3%B8nn.pdf",
//    "publishedDate": "2024-07-01T10:31:11.782146995Z",
//    "publisherVersion": "PublishedVersion",
//    "rightsRetentionStrategy": {
//    "type": "NullRightsRetentionStrategy",
//    "configuredType": "Unknown"
//    },
//    "size": 181359,
//    "uploadDetails": {
//    "type": "UploadDetails",
//    "uploadedBy": "uis@217.0.0.0",
//    "uploadedDate": "2024-07-01T10:31:11.578030080Z"
//    },
//    "visibleForNonOwner": true
//    }
//    ],
//    "contributorOrganizations": [
//    "https://api.dev.nva.aws.unit.no/cristin/organization/217.0.0.0",
//    "https://api.dev.nva.aws.unit.no/cristin/organization/217.13.0.0",
//    "https://api.dev.nva.aws.unit.no/cristin/organization/217.13.3.0"
//    ],
//    "createdDate": "2022-12-19T00:00:00Z",
//    "curatingInstitutions": [
//    "https://api.dev.nva.aws.unit.no/cristin/organization/217.0.0.0"
//    ],
//    "entityDescription": {
//    "type": "EntityDescription",
//    "abstract": "I denne artikkelen illustrerer jeg hvordan en psykososial tilnærming (Salling Olesen 2020) som
//    teori og metode girinnganger til å forstå hvordan to teologistudenters utdanningsvalg som prester inngår i
//    deres livshistorier. Ved åundersøke teologistudentenes livshistorier åpner det for tolkninger av hvorledes
//    deres sosialisering til presteskapet erinnleiret i bredere samfunnsmessige prosesser, som moderniseringen av
//    det norske samfunnet og utviklingen av like-stilling. Jeg viser hvordan teologistudentenes kjønnede
//    sosialiserte subjektivitet inngår i deres utvikling av en profe-sjonell identitet som prest",
//    "alternativeAbstracts": {},
//    "contributors": [
//    {
//    "type": "Contributor",
//    "affiliations": [
//    {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/organization/217.13.3.0",
//    "type": "Organization",
//    "countryCode": "NO",
//    "labels": {
//    "nb": "Avdeling for omsorg og etikk",
//    "en": "Department of Caring and Ethics"
//    }
//    }
//    ],
//    "correspondingAuthor": false,
//    "identity": {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/person/18461",
//    "type": "Identity",
//    "name": "Sissel Merete Finholt-Pedersen",
//    "verificationStatus": "Verified"
//    },
//    "role": {
//    "type": "Creator"
//    },
//    "sequence": 1
//    }
//    ],
//    "language": "http://lexvo.org/id/iso639-3/nor",
//    "mainTitle": "Å formes som prest - en psykososial tilnærming til profesjonell identitet, subjektivitet og kjønn.",
//    "publicationDate": {
//    "type": "PublicationDate",
//    "day": "16",
//    "month": "12",
//    "year": "2022"
//    },
//    "reference": {
//    "type": "Reference",
//    "doi": "https://doi.org/10.18261/tfk.46.3.6",
//    "publicationContext": {
//    "id": "https://api.dev.nva.aws.unit.no/publication-channels-v2/journal/A0314BCC-06E0-447A-B2A5-EDB85B79BBC8/2022",
//    "type": "Journal",
//    "identifier": "A0314BCC-06E0-447A-B2A5-EDB85B79BBC8",
//    "name": "Tidsskrift for kjønnsforskning",
//    "onlineIssn": "1891-1781",
//    "printIssn": "0809-6341",
//    "sameAs": "https://kanalregister.hkdir.no/publiseringskanaler/KanalTidsskriftInfo?pid=A0314BCC-06E0-447A-B2A5-EDB85B79BBC8",
//    "scientificValue": "LevelOne"
//    },
//    "publicationInstance": {
//    "type": "AcademicArticle",
//    "articleNumber": "5",
//    "issue": "3-4",
//    "pages": {
//    "type": "Range",
//    "begin": "180",
//    "end": "193"
//    },
//    "volume": "46"
//    }
//    }
//    },
//    "fundings": [
//    {
//    "id": "https://api.dev.nva.aws.unit.no/verified-funding/nfr/NFR301827",
//    "type": "ConfirmedFunding",
//    "identifier": "NFR301827",
//    "source": {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/funding-sources/NFR",
//    "type": "FundingSource",
//    "identifier": "NFR",
//    "labels": {
//    "nb": "Norges forskningsråd",
//    "en": "Research Council of Norway (RCN)"
//    },
//    "name": {
//    "en": "Research Council of Norway (RCN)",
//    "nb": "Norges forskningsråd"
//    }
//    }
//    },
//    {
//    "type": "UnconfirmedFunding",
//    "identifier": "IN-10208",
//    "source": {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/funding-sources/UIS",
//    "type": "FundingSource",
//    "identifier": "UIS",
//    "labels": {
//    "nb": "Universitetet i Stavanger",
//    "en": "University of Stavanger"
//    },
//    "name": {
//    "en": "University of Stavanger",
//    "nb": "Universitetet i Stavanger"
//    }
//    }
//    }
//    ],
//    "handle": "https://hdl.handle.net/11250/3039107",
//    "identifier": "019055007609-40253705-d66c-47bc-bae1-295f9d10d0ce",
//    "importDetail": [
//    {
//    "type": "ImportDetail",
//    "importDate": "2024-07-09T15:06:38.260090303Z",
//    "importSource": {
//    "type": "ImportSource"
//    }
//    },
//    {
//    "type": "ImportDetail",
//    "importDate": "2024-06-26T14:43:31.977105171Z",
//    "importSource": {
//    "type": "ImportSource"
//    }
//    },
//    {
//    "type": "ImportDetail",
//    "importDate": "2024-07-01T10:31:12.196119064Z",
//    "importSource": {
//    "type": "ImportSource",
//    "archive": "uis"
//    }
//    }
//    ],
//    "modelVersion": "0.23.2",
//    "modifiedDate": "2024-07-09T15:06:38.268913852Z",
//    "publishedDate": "2022-12-19T00:00:00Z",
//    "publisher": {
//    "id": "https://api.dev.nva.aws.unit.no/customer/bb3d0c0c-5065-4623-9b98-5810983c2478",
//    "type": "Organization"
//    },
//    "resourceOwner": {
//    "owner": "uis@217.0.0.0",
//    "ownerAffiliation": "https://api.dev.nva.aws.unit.no/cristin/organization/217.0.0.0"
//    },
//    "rightsHolder": "The author",
//    "scientificIndex": {
//    "id": "https://api.dev.nva.aws.unit.no/scientific-index/period/2022",
//    "type": "ScientificIndex",
//    "status": "Reported",
//    "year": "2022"
//    },
//    "status": "PUBLISHED",
//    "topLevelOrganizations": [
//    {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/organization/217.0.0.0",
//    "type": "Organization",
//    "countryCode": "NO",
//    "hasPart": [
//    {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/organization/217.13.0.0",
//    "type": "Organization",
//    "countryCode": "NO",
//    "hasPart": [
//    {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/organization/217.13.3.0",
//    "type": "Organization",
//    "countryCode": "NO",
//    "labels": {
//    "nb": "Avdeling for omsorg og etikk",
//    "en": "Department of Caring and Ethics"
//    },
//    "partOf": {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/organization/217.13.0.0"
//    }
//    }
//    ],
//    "labels": {
//    "nb": "Det helsevitenskapelige fakultet",
//    "en": "Faculty of Health Sciences"
//    },
//    "partOf": {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/organization/217.0.0.0"
//    }
//    }
//    ],
//    "labels": {
//    "nb": "Universitetet i Stavanger",
//    "en": "University of Stavanger"
//    }
//    }
//    ],
//    "filesStatus": "hasPublicFiles"
//    }

public record PublicationIndexDocument(String type,
                                       @JsonProperty("@context") String context,
                                       String id,
                                       EntityDescription entityDescription,
                                       String identifier,
                                       String modifiedDate,
                                       String status,
                                       List<Funding> fundings) implements JsonSerializable {

    public static final String EN = "en";
    private static final String TYPE = "Publication";
    private static final String CONTEXT = "https://api.dev.nva.aws.unit.no/publication/context";

    public static PublicationIndexDocument from(TestPublication publication) {
        return new PublicationIndexDocument(
            TYPE,
            CONTEXT,
            publication.getPublicationUri(),
            EntityDescription.from(publication),
            publication.getIdentifier(),
            publication.getModifiedDate().toString(),
            publication.getPublicationStatus(),
            publication.getFundings().stream().map(Funding::from).toList()
        );
    }

    public JsonNode asJsonNode() {
        return attempt(() -> dtoObjectMapper.readTree(this.toJsonString())).orElseThrow();
    }

    private record TestOrganization(String id,
                                    String type,
                                    Map<String, String> labels,
                                    TestOrganization partOf,
                                    TestOrganization hasPart) {

        public static final String TYPE = "Organization";
        public static final String NB = "nb";

        public static TestOrganization from(
            no.sikt.nva.data.report.testing.utils.generator.publication.TestOrganization testOrganization) {
            return new TestOrganization(
                testOrganization.getId(),
                TYPE,
                isNull(testOrganization.getName()) ? null : Map.of(NB, testOrganization.getName()),
                testOrganization.getPartOf().map(TestOrganization::from).orElse(null),
                null
            );
        }
    }

    private record EntityDescription(String type,
                                     List<Contributor> contributors,
                                     String mainTitle,
                                     PublicationDate publicationDate,
                                     Reference reference) {

        public static final String TYPE = "EntityDescription";

        public static EntityDescription from(TestPublication publication) {
            return new EntityDescription(TYPE,
                                         publication.getContributors().stream().map(Contributor::from).toList(),
                                         publication.getMainTitle(),
                                         PublicationDate.from(publication.getDate()),
                                         Reference.from(publication));
        }

        private record Contributor(String type,
                                   Identity identity,
                                   Role role,
                                   String sequence,
                                   List<Affiliation> affiliations) {

            public static final String TYPE = "Contributor";

            public static Contributor from(TestContributor testContributor) {
                return new Contributor(
                    TYPE,
                    Identity.from(testContributor.getIdentity()),
                    new Role(testContributor.role()),
                    testContributor.getSequenceNumber(),
                    testContributor.getAffiliations().stream().map(Affiliation::from).toList()
                );
            }

            private record Identity(String type,
                                    String name,
                                    String id) {

                public static final String TYPE = "Identity";

                public static Identity from(TestIdentity identity) {
                    return new Identity(
                        TYPE,
                        identity.name(),
                        identity.uri()
                    );
                }
            }

            private record Role(String type) {

            }

            private record Affiliation(String id,
                                       String type,
                                       Map<String, String> labels,
                                       Affiliation partOf) {

                public static final String TYPE = "Organization";
                public static final String NB = "nb";

                public static Affiliation from(
                    no.sikt.nva.data.report.testing.utils.generator.publication.TestOrganization testOrganization) {
                    return new Affiliation(
                        testOrganization.getId(),
                        TYPE,
                        isNull(testOrganization.getName()) ? null : Map.of(NB, testOrganization.getName()),
                        testOrganization.getPartOf().map(Affiliation::from).orElse(null)
                    );
                }
            }
        }

        private record PublicationDate(String type,
                                       String year,
                                       String month,
                                       String day) {

            public static final String TYPE = "PublicationDate";

            public static PublicationDate from(
                no.sikt.nva.data.report.testing.utils.generator.publication.PublicationDate date) {
                return new PublicationDate(TYPE,
                                           date.year(),
                                           date.month(),
                                           date.day());
            }
        }

        private record Reference(String type,
                                 PublicationContext publicationContext,
                                 PublicationInstance publicationInstance) {

            public static final String TYPE = "Reference";

            public static Reference from(TestPublication publication) {
                return new Reference(TYPE,
                                     PublicationContext.from(publication),
                                     PublicationInstance.from(publication));
            }

            private record PublicationContext(String type,
                                              String identifier,
                                              String name,
                                              String onlineIssn,
                                              String printIssn,
                                              String scientificValue) {

                public static PublicationContext from(TestPublication publication) {
                    return new PublicationContext(
                        publication.getChannel().getType(),
                        publication.getChannel().getIdentifier(),
                        publication.getChannel().getName(),
                        publication.getChannel().getOnlineIssn(),
                        publication.getChannel().getPrintIssn(),
                        publication.getChannel().getScientificValue()
                    );
                }
            }

            private record PublicationInstance(String type) {

                public static PublicationInstance from(TestPublication publication) {
                    return new PublicationInstance(publication.getPublicationCategory());
                }
            }
        }
    }

    private record Funding(String type,
                           String id,
                           String identifier,
                           FundingSource source) {

        public static final String IRRELEVANT_HARDCODED_FUNDING_TYPE = "ConfirmedFunding";

        public static Funding from(TestFunding testFunding) {
            return new Funding(
                IRRELEVANT_HARDCODED_FUNDING_TYPE,
                testFunding.getId(),
                UriWrapper.fromUri(testFunding.getId()).getLastPathElement(),
                FundingSource.from(testFunding)
            );
        }

        private record FundingSource(String identifier,
                                     String type,
                                     Map<String, String> labels) {

            public static final String TYPE = "FundingSource";

            public static FundingSource from(TestFunding testFunding) {
                return new FundingSource(
                    testFunding.getFundingSource(),
                    TYPE,
                    Map.of(EN, testFunding.getName())
                );
            }
        }
    }
}

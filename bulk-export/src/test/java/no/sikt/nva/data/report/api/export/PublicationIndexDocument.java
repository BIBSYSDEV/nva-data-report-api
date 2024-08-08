package no.sikt.nva.data.report.api.export;

import static java.util.Objects.isNull;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestContributor;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestIdentity;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestPublication;
import no.unit.nva.commons.json.JsonSerializable;

//{
//    "type": "Publication",
//    "publicationContextUris": [
//    "https://api.dev.nva.aws.unit.no/publication-channels-v2/journal/39AD7019-587C-49B1-94D6-8D354354A2F8/2003"
//    ],
//    "@context": "https://api.dev.nva.aws.unit.no/publication/context",
//    "id": "https://api.dev.nva.aws.unit.no/publication/019054ff1d08-2727f611-dd15-4b54-ae68-713bed4a5893",
//    "additionalIdentifiers": [
//    {
//    "type": "AdditionalIdentifier",
//    "sourceName": "Cristin",
//    "value": "407570"
//    },
//    {
//    "type": "AdditionalIdentifier",
//    "sourceName": "BIBSYS",
//    "value": "r04005433"
//    }
//    ],
//    "contributorOrganizations": [
//    "https://api.dev.nva.aws.unit.no/cristin/organization/194.63.0.0",
//    "https://api.dev.nva.aws.unit.no/cristin/organization/194.63.35.0",
//    "https://api.dev.nva.aws.unit.no/cristin/organization/194.0.0.0"
//    ],
//    "createdDate": "2004-01-16T00:00:00Z",
//    "curatingInstitutions": [
//    "https://api.dev.nva.aws.unit.no/cristin/organization/194.0.0.0"
//    ],
//    "entityDescription": {
//    "type": "EntityDescription",
//    "alternativeAbstracts": {},
//    "contributors": [
//    {
//    "type": "Contributor",
//    "correspondingAuthor": false,
//    "identity": {
//    "type": "Identity",
//    "name": "Eli Janne Fiskerstrand",
//    "verificationStatus": "NotVerified"
//    },
//    "role": {
//    "type": "Creator"
//    },
//    "sequence": 1
//    },
//    {
//    "type": "Contributor",
//    "affiliations": [
//    {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/organization/194.63.35.0",
//    "type": "Organization",
//    "countryCode": "NO",
//    "labels": {
//    "nb": "Institutt for elektroniske systemer",
//    "en": "Department of Electronic Systems"
//    }
//    }
//    ],
//    "correspondingAuthor": false,
//    "identity": {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/person/40964",
//    "type": "Identity",
//    "name": "Lars O. Svaasand",
//    "verificationStatus": "Verified"
//    },
//    "role": {
//    "type": "Creator"
//    },
//    "sequence": 2
//    },
//    {
//    "type": "Contributor",
//    "correspondingAuthor": false,
//    "identity": {
//    "type": "Identity",
//    "name": "J. Stuart Nelson",
//    "verificationStatus": "NotVerified"
//    },
//    "role": {
//    "type": "Creator"
//    },
//    "sequence": 3
//    }
//    ],
//    "language": "http://lexvo.org/id/iso639-3/eng",
//    "mainTitle": "Hair removal with long pulsed diode lasers; a comparison between two systems with different pulse "
//    + "structures",
//    "publicationDate": {
//    "type": "PublicationDate",
//    "year": "2003"
//    },
//    "reference": {
//    "type": "Reference",
//    "publicationContext": {
//    "id": "https://api.dev.nva.aws.unit.no/publication-channels-v2/journal/39AD7019-587C-49B1-94D6-8D354354A2F8/2003",
//    "type": "Journal",
//    "identifier": "39AD7019-587C-49B1-94D6-8D354354A2F8",
//    "name": "Lasers in Surgery and Medicine",
//    "onlineIssn": "1096-9101",
//    "printIssn": "0196-8092",
//    "sameAs": "https://kanalregister.hkdir.no/publiseringskanaler/KanalTidsskriftInfo?pid=39AD7019-587C-49B1-94D6"
//    + "-8D354354A2F8",
//    "scientificValue": "Unassigned"
//    },
//    "publicationInstance": {
//    "type": "AcademicArticle",
//    "pages": {
//    "type": "Range"
//    }
//    }
//    }
//    },
//    "identifier": "019054ff1d08-2727f611-dd15-4b54-ae68-713bed4a5893",
//    "importDetail": {
//    "type": "ImportDetail",
//    "importDate": "2024-06-26T14:42:03.656937021Z",
//    "importSource": {
//    "type": "ImportSource"
//    }
//    },
//    "modelVersion": "0.22.2",
//    "modifiedDate": "2013-10-21T00:00:00Z",
//    "publishedDate": "2004-01-16T00:00:00Z",
//    "publisher": {
//    "id": "https://api.dev.nva.aws.unit.no/customer/bb3d0c0c-5065-4623-9b98-5810983c2478",
//    "type": "Organization"
//    },
//    "resourceOwner": {
//    "owner": "ntnu@194.0.0.0",
//    "ownerAffiliation": "https://api.dev.nva.aws.unit.no/cristin/organization/194.0.0.0"
//    },
//    "status": "PUBLISHED",
//    "topLevelOrganizations": [
//    {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/organization/194.0.0.0",
//    "type": "Organization",
//    "countryCode": "NO",
//    "hasPart": {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/organization/194.63.0.0",
//    "type": "Organization",
//    "countryCode": "NO",
//    "hasPart": {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/organization/194.63.35.0",
//    "type": "Organization",
//    "countryCode": "NO",
//    "labels": {
//    "nb": "Institutt for elektroniske systemer",
//    "en": "Department of Electronic Systems"
//    },
//    "partOf": {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/organization/194.63.0.0"
//    }
//    },
//    "labels": {
//    "nb": "Fakultet for informasjonsteknologi og elektroteknikk",
//    "en": "Faculty of Information Technology and Electrical Engineering"
//    },
//    "partOf": {
//    "id": "https://api.dev.nva.aws.unit.no/cristin/organization/194.0.0.0"
//    }
//    },
//    "labels": {
//    "nn": "Noregs teknisk-naturvitskaplege universitet",
//    "nb": "Norges teknisk-naturvitenskapelige universitet",
//    "en": "Norwegian University of Science and Technology"
//    }
//    }
//    ],
//    "filesStatus": "noFiles"
//    }

public record PublicationIndexDocument(String type,
                                       @JsonProperty("@context") String context,
                                       String id,
                                       EntityDescription entityDescription,
                                       String identifier,
                                       String modifiedDate,
                                       String status) implements JsonSerializable {

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
            publication.getPublicationStatus()
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
                                   List<Affiliation> affiliations) {

            public static final String TYPE = "Contributor";

            public static Contributor from(TestContributor testContributor) {
                return new Contributor(
                    TYPE,
                    Identity.from(testContributor.getIdentity()),
                    new Role(testContributor.role()),
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
}

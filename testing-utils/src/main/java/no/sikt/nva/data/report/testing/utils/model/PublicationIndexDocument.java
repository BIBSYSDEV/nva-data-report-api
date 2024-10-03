package no.sikt.nva.data.report.testing.utils.model;

import static java.util.Objects.isNull;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.sikt.nva.data.report.testing.utils.generator.publication.SampleAdditionalIdentifier;
import no.sikt.nva.data.report.testing.utils.generator.publication.SampleContributor;
import no.sikt.nva.data.report.testing.utils.generator.publication.SampleFunding;
import no.sikt.nva.data.report.testing.utils.generator.publication.SampleIdentity;
import no.sikt.nva.data.report.testing.utils.generator.publication.SampleOrganization;
import no.sikt.nva.data.report.testing.utils.generator.publication.SamplePublication;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.paths.UriWrapper;

public record PublicationIndexDocument(String type,
                                       @JsonProperty("@context") String context,
                                       String id,
                                       EntityDescription entityDescription,
                                       String identifier,
                                       String modifiedDate,
                                       String status,
                                       List<Funding> fundings,
                                       List<AdditionalIdentifier> additionalIdentifiers,
                                       List<TestOrganization> topLevelOrganizations) implements JsonSerializable {

    public static final String EN = "en";
    private static final String TYPE = "Publication";
    private static final String CONTEXT = "https://api.dev.nva.aws.unit.no/publication/context";

    public static PublicationIndexDocument from(SamplePublication publication) {
        return new PublicationIndexDocument(
            TYPE,
            CONTEXT,
            publication.getPublicationUri(),
            EntityDescription.from(publication),
            publication.getIdentifier(),
            publication.getModifiedDate().toString(),
            publication.getPublicationStatus(),
            publication.getFundings().stream().map(Funding::from).toList(),
            publication.getAdditionalIdentifiers().stream().map(AdditionalIdentifier::from).toList(),
            generateTopLevelOrganizations(publication)
        );
    }

    public JsonNode asJsonNode() {
        return attempt(() -> dtoObjectMapper.readTree(this.toJsonString())).orElseThrow();
    }

    public IndexDocument toIndexDocument() {
        return IndexDocument.from(this);
    }

    private static List<TestOrganization> generateTopLevelOrganizations(SamplePublication publication) {
        return publication.getContributorAffiliations()
                   .stream()
                   .map(PublicationIndexDocument::generateTopLevelOrganization)
                   .map(json -> attempt(() -> dtoObjectMapper.readValue(json, TestOrganization.class)).orElseThrow())
                   .toList();
    }

    private static String generateTopLevelOrganization(
        SampleOrganization affiliation) {
        // Struggled to recreate the nested structure for top level organization based on affiliation, therefore
        // hardcoded
        return String.format("""
                                 {
                                   "id": "https://example.org/organization/10.0.0.0",
                                   "type": "Organization",
                                   "hasPart": {
                                     "id": "https://example.org/organization/10.1.0.0",
                                     "type": "Organization",
                                     "hasPart": {
                                       "id": "https://example.org/organization/10.1.1.0",
                                       "type": "Organization",
                                       "hasPart": {
                                         "id": "%s",
                                         "type": "Organization",
                                         "labels": {
                                           "en": "%s"
                                         }
                                       }
                                     }
                                   }
                                 }
                                 """, affiliation.getId(), affiliation.getName());
    }

    private record TestOrganization(String id,
                                    String type,
                                    Map<String, String> labels,
                                    TestOrganization partOf,
                                    TestOrganization hasPart) {

    }

    private record EntityDescription(String type,
                                     List<Contributor> contributors,
                                     String mainTitle,
                                     PublicationDate publicationDate,
                                     Reference reference) {

        public static final String TYPE = "EntityDescription";

        public static EntityDescription from(SamplePublication publication) {
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

            public static Contributor from(SampleContributor sampleContributor) {
                return new Contributor(
                    TYPE,
                    Identity.from(sampleContributor.getIdentity()),
                    new Role(sampleContributor.role()),
                    sampleContributor.getSequenceNumber(),
                    sampleContributor.getAffiliations().stream().map(Affiliation::from).toList()
                );
            }

            private record Identity(String type,
                                    String name,
                                    String id) {

                public static final String TYPE = "Identity";

                public static Identity from(SampleIdentity identity) {
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

                public static Affiliation from(
                    SampleOrganization sampleOrganization) {
                    return new Affiliation(
                        sampleOrganization.getId(),
                        TYPE,
                        isNull(sampleOrganization.getName()) ? null : Map.of(EN, sampleOrganization.getName()),
                        sampleOrganization.getPartOf().map(Affiliation::from).orElse(null)
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

            public static Reference from(SamplePublication publication) {
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

                public static PublicationContext from(SamplePublication publication) {
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

                public static PublicationInstance from(SamplePublication publication) {
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

        public static Funding from(SampleFunding sampleFunding) {
            return new Funding(
                IRRELEVANT_HARDCODED_FUNDING_TYPE,
                sampleFunding.getId(),
                Optional.ofNullable(sampleFunding.getId()).map(Funding::getLastPathElement).orElse(null),
                FundingSource.from(sampleFunding)
            );
        }

        private static String getLastPathElement(String uri) {
            return UriWrapper.fromUri(uri).getLastPathElement();
        }

        private record FundingSource(String identifier,
                                     String type,
                                     Map<String, String> labels) {

            public static final String TYPE = "FundingSource";

            public static FundingSource from(SampleFunding sampleFunding) {
                return new FundingSource(
                    sampleFunding.getFundingSource(),
                    TYPE,
                    Map.of(EN, sampleFunding.getName())
                );
            }
        }
    }

    private record AdditionalIdentifier(String sourceName,
                                        String value,
                                        String type) {

        public static AdditionalIdentifier from(SampleAdditionalIdentifier additionalIdentifier) {
            return new AdditionalIdentifier(additionalIdentifier.getSourceName(), additionalIdentifier.getValue(),
                                            additionalIdentifier.getType());
        }
    }
}

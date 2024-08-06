package no.sikt.nva.data.report.testing.utils.generator;

import java.util.UUID;

public class Constants {

    public static final String ONTOLOGY_BASE_URI = "https://nva.sikt.no/ontology/publication#";
    private static final String BASE_URI = "https://example.org/";
    public static final String PUBLICATION_BASE_URI = BASE_URI + "publication/";
    public static final String PERSON_BASE_URI = BASE_URI + "person/";
    public static final String NVI_CANDIDATE_BASE_URI = BASE_URI + "scientific-index/candidate/";
    public static final String ORGANIZATION_BASE_URI = BASE_URI + "organization/";
    public static final String FUNDING_SOURCE_BASE_URI = BASE_URI + "funding/";
    private static final String VERIFIED_FUNDING = BASE_URI + "verified-funding/nfr/";
    private static final String PUBLISHER_BASE_URI = BASE_URI + "publication-channels-v2/publisher/";
    private static final String JOURNAL_BASE_URI = BASE_URI + "publication-channels-v2/journal/";
    private static final String YEAR_PATH = "/2023";

    private Constants() {
    }

    public static String organizationUri(String localName) {
        return ORGANIZATION_BASE_URI + localName;
    }

    public static String verifiedFundingUri(int localName) {
        return VERIFIED_FUNDING + localName;
    }

    public static String fundingSourceUri(String localName) {
        return FUNDING_SOURCE_BASE_URI + localName;
    }

    public static String publisherUri(UUID localName) {
        return PUBLISHER_BASE_URI + localName;
    }

    public static String journalUri(UUID localName) {
        return JOURNAL_BASE_URI + localName + YEAR_PATH;
    }

    public static String publicationUri(UUID localName) {
        return PUBLICATION_BASE_URI + localName;
    }

    public static String candidateUri(UUID localName) {
        return NVI_CANDIDATE_BASE_URI + localName;
    }

    public static String ontologyUri(String localName) {
        return ONTOLOGY_BASE_URI + localName;
    }

    public static String person(String localName) {
        return PERSON_BASE_URI + localName;
    }
}

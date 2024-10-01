package no.sikt.nva.data.report.testing.utils.generator;

import static no.sikt.nva.data.report.testing.utils.generator.Constants.organizationUri;
import static no.sikt.nva.data.report.testing.utils.generator.NviSampleData.generateNviCandidate;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.ADDITIONAL_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.ADDITIONAL_IDENTIFIER_SOURCE;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.ADDITIONAL_IDENTIFIER_TYPE;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.AFFILIATION_ID;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.AFFILIATION_NAME;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CHANNEL_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CHANNEL_LEVEL;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CHANNEL_NAME;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CHANNEL_ONLINE_ISSN;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CHANNEL_PRINT_ISSN;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CHANNEL_TYPE;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CONTRIBUTOR_ID;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CONTRIBUTOR_NAME;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CONTRIBUTOR_ROLE;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CONTRIBUTOR_SEQUENCE_NUMBER;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.DEPARTMENT_ID;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.FACULTY_ID;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.FUNDING_ID;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.FUNDING_NAME;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.FUNDING_SOURCE;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.GROUP_ID;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.INSTITUTION_ID;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.MODIFIED_DATE;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_CATEGORY;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_DATE;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_ID;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_TITLE;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.STATUS;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleNviCandidate;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleNviContributor;
import no.sikt.nva.data.report.testing.utils.generator.publication.PublicationDate;
import no.sikt.nva.data.report.testing.utils.generator.publication.SampleAdditionalIdentifier;
import no.sikt.nva.data.report.testing.utils.generator.publication.SampleChannel;
import no.sikt.nva.data.report.testing.utils.generator.publication.SampleContributor;
import no.sikt.nva.data.report.testing.utils.generator.publication.SampleFunding;
import no.sikt.nva.data.report.testing.utils.generator.publication.SampleIdentity;
import no.sikt.nva.data.report.testing.utils.generator.publication.SampleOrganization;
import no.sikt.nva.data.report.testing.utils.generator.publication.SamplePublication;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public class SampleData {

    public static final String SOME_TOP_LEVEL_IDENTIFIER = "10.0.0.0";
    public static final String SOME_SUB_UNIT_IDENTIFIER = "10.1.1.2";
    private static final String DELIMITER = ",";
    private static final List<String> AFFILIATION_HEADERS = List.of(PUBLICATION_ID, STATUS,
                                                                    PUBLICATION_IDENTIFIER,
                                                                    CONTRIBUTOR_ID,
                                                                    CONTRIBUTOR_NAME,
                                                                    AFFILIATION_ID,
                                                                    AFFILIATION_NAME,
                                                                    INSTITUTION_ID, FACULTY_ID,
                                                                    DEPARTMENT_ID, GROUP_ID,
                                                                    MODIFIED_DATE);
    private static final List<String> FUNDING_HEADERS = List.of(PUBLICATION_ID, STATUS,
                                                                PUBLICATION_IDENTIFIER,
                                                                FUNDING_SOURCE,
                                                                FUNDING_ID, FUNDING_NAME,
                                                                MODIFIED_DATE);
    private static final List<String> PUBLICATION_HEADERS = List.of(PUBLICATION_ID, STATUS,
                                                                    PUBLICATION_TITLE,
                                                                    PUBLICATION_CATEGORY,
                                                                    PUBLICATION_DATE,
                                                                    CHANNEL_TYPE,
                                                                    CHANNEL_IDENTIFIER,
                                                                    CHANNEL_NAME,
                                                                    CHANNEL_ONLINE_ISSN,
                                                                    CHANNEL_PRINT_ISSN,
                                                                    CHANNEL_LEVEL,
                                                                    PUBLICATION_IDENTIFIER,
                                                                    MODIFIED_DATE);
    private static final List<String> CONTRIBUTOR_HEADERS = List.of(PUBLICATION_ID, STATUS,
                                                                    PUBLICATION_IDENTIFIER,
                                                                    CONTRIBUTOR_IDENTIFIER,
                                                                    CONTRIBUTOR_NAME,
                                                                    CONTRIBUTOR_SEQUENCE_NUMBER,
                                                                    CONTRIBUTOR_ROLE,
                                                                    MODIFIED_DATE);
    private static final List<String> IDENTIFIER_HEADERS = List.of(PUBLICATION_ID, STATUS,
                                                                   PUBLICATION_IDENTIFIER,
                                                                   ADDITIONAL_IDENTIFIER_SOURCE,
                                                                   ADDITIONAL_IDENTIFIER,
                                                                   ADDITIONAL_IDENTIFIER_TYPE,
                                                                   MODIFIED_DATE);
    private final List<SamplePublication> publicationTestData;
    private final List<SampleNviCandidate> nviTestData;

    public SampleData(List<DatePair> dates) {
        this.publicationTestData = generatePublicationData(dates);
        this.nviTestData = NviSampleData.generateNviData(publicationTestData);
    }

    public SampleData() {
        var publication = generatePublication(new PublicationDate("2024", "02", "02"), Instant.now());
        publicationTestData = generatePublicationData(publication);
        this.nviTestData = generateNviData(publication);
    }

    public List<SamplePublication> getPublicationTestData() {
        return publicationTestData;
    }

    public List<SampleNviCandidate> getNviTestData() {
        return nviTestData;
    }

    public String getAffiliationResponseData() {
        var headers = String.join(DELIMITER, AFFILIATION_HEADERS) + CRLF.getString();
        publicationTestData.sort(this::sortByPublicationUri);
        var values = publicationTestData.stream()
                         .map(SamplePublication::getExpectedAffiliationResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getContributorResponseData() {
        var headers = String.join(DELIMITER, CONTRIBUTOR_HEADERS) + CRLF.getString();
        publicationTestData.sort(this::sortByPublicationUri);
        var values = publicationTestData.stream()
                         .map(SamplePublication::getExpectedContributorResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getFundingResponseData() {
        var headers = String.join(DELIMITER, FUNDING_HEADERS) + CRLF.getString();
        publicationTestData.sort(this::sortByPublicationUri);
        var values = publicationTestData.stream()
                         .map(SamplePublication::getExpectedFundingResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getIdentifierResponseData() {
        var headers = String.join(DELIMITER, IDENTIFIER_HEADERS) + CRLF.getString();
        publicationTestData.sort(this::sortByPublicationUri);
        var values = publicationTestData.stream()
                         .map(SamplePublication::getExpectedIdentifierResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getPublicationResponseData() {
        var headers = String.join(DELIMITER, PUBLICATION_HEADERS) + CRLF.getString();
        publicationTestData.sort(this::sortByPublicationUri);
        var values = publicationTestData.stream()
                         .map(SamplePublication::getExpectedPublicationResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getNviResponseData() {
        var headers = String.join(DELIMITER, NviSampleData.NVI_HEADERS) + CRLF.getString();
        nviTestData.sort(this::sortByPublicationUri);
        sortContributors(nviTestData);
        var values = nviTestData.stream()
                         .map(SampleNviCandidate::getExpectedResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    private static List<SampleNviCandidate> generateNviData(SamplePublication publication) {
        var nviDataSet = new ArrayList<SampleNviCandidate>();
        nviDataSet.add(generateNviCandidate(Instant.now(), publication));
        return nviDataSet;
    }

    private static SamplePublication generatePublication(PublicationDate date, Instant modifiedDate) {
        var identifier = UUID.randomUUID();
        return new SamplePublication()
                   .withModifiedDate(modifiedDate)
                   .withPublicationStatus("PUBLISHED")
                   .withPublicationDate(date)
                   .withPublicationUri(Constants.publicationUri(identifier))
                   .withPublicationIdentifier(identifier.toString())
                   .withContributors(List.of(generateContributor()))
                   .withFundings(List.of(generateFunding(), generateFundingWithoutId()))
                   .withPublicationTitle("My study")
                   .withPublicationCategory("AcademicArticle")
                   .withPublicationDate(date)
                   .withChannel(generateChannel())
                   .withAdditionalIdentifiers(List.of(generateAdditionalIdentifier()));
    }

    private static SampleAdditionalIdentifier generateAdditionalIdentifier() {
        return new SampleAdditionalIdentifier()
                   .withSourceName("Cristin")
                   .withValue(randomString())
                   .withType("CristinIdentifier");
    }

    private static SampleChannel generateChannel() {
        return new SampleChannel()
                   .withType("Journal")
                   .withIdentifier(UUID.randomUUID())
                   .withName("Journal of Studies")
                   .withOnlineIssn("1111-1111")
                   .withPrintIssn("1212-122X")
                   .withScientificValue("LevelTwo");
    }

    private static SampleFunding generateFunding() {
        return new SampleFunding()
                   .withName("My big funding")
                   .withId(randomUri() + "/my-funding-1")
                   .withFundingSource("NFR")
                   .withName("Research Council of Norway");
    }

    private static SampleFunding generateFundingWithoutId() {
        return new SampleFunding()
                   .withName("My big funding")
                   .withFundingSource("NFR")
                   .withName("Research Council of Norway");
    }

    private static SampleContributor generateContributor() {
        return new SampleContributor()
                   .withIdentity(new SampleIdentity(Constants.person("54431"), "Jim Person"))
                   .withContributorSequenceNo("1")
                   .withContributorRole("Creator")
                   .withAffiliations(List.of(generateAffiliation()));
    }

    private static SampleOrganization generateAffiliation() {
        return new SampleOrganization(organizationUri(SOME_SUB_UNIT_IDENTIFIER), "My university");
    }

    private List<SamplePublication> generatePublicationData(SamplePublication publication) {
        var publicationDataSet = new ArrayList<SamplePublication>();
        publicationDataSet.add(publication);
        return publicationDataSet;
    }

    private void sortContributors(List<SampleNviCandidate> expectedCandidates) {
        expectedCandidates.forEach(candidate -> candidate.publicationDetails()
                                                    .contributors()
                                                    .sort(this::sortByContributor));
    }

    private List<SamplePublication> generatePublicationData(List<DatePair> dates) {
        var dataSet = new ArrayList<SamplePublication>();
        for (DatePair date : dates) {
            var data = generatePublication(date.publicationDate, date.modifiedDate);
            dataSet.add(data);
        }
        return dataSet;
    }

    private int sortByPublicationUri(SamplePublication a, SamplePublication b) {
        return a.getPublicationUri().compareTo(b.getPublicationUri());
    }

    private int sortByPublicationUri(SampleNviCandidate a, SampleNviCandidate b) {
        return a.publicationDetails().id().compareTo(b.publicationDetails().id());
    }

    private int sortByContributor(SampleNviContributor a, SampleNviContributor b) {
        return a.id().compareTo(b.id());
    }

    public record DatePair(PublicationDate publicationDate, Instant modifiedDate) {

    }
}

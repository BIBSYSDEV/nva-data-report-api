package no.sikt.nva.data.report.api.fetch.testutils.generator;

import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestAffiliation;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviCandidate;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviContributor;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestPublicationDetails;
import no.sikt.nva.data.report.api.fetch.testutils.generator.publication.PublicationDate;
import no.sikt.nva.data.report.api.fetch.testutils.generator.publication.TestChannel;
import no.sikt.nva.data.report.api.fetch.testutils.generator.publication.TestContributor;
import no.sikt.nva.data.report.api.fetch.testutils.generator.publication.TestFunding;
import no.sikt.nva.data.report.api.fetch.testutils.generator.publication.TestIdentity;
import no.sikt.nva.data.report.api.fetch.testutils.generator.publication.TestOrganization;
import no.sikt.nva.data.report.api.fetch.testutils.generator.publication.TestPublication;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class TestData {

    private static final String PUBLICATION_ID = "publicationId";
    private static final String PUBLICATION_IDENTIFIER = "publicationIdentifier";
    private static final String PUBLICATION_CATEGORY = "publicationCategory";
    private static final String PUBLICATION_TITLE = "publicationTitle";
    private static final String CONTRIBUTOR_SEQUENCE_NUMBER = "contributorSequenceNumber";
    private static final String CONTRIBUTOR_ROLE = "contributorRole";
    private static final String CONTRIBUTOR_ID = "contributorId";
    private static final String CONTRIBUTOR_IDENTIFIER = "contributorIdentifier";
    private static final String CONTRIBUTOR_NAME = "contributorName";
    private static final String AFFILIATION_ID = "affiliationId";
    private static final String AFFILIATION_NAME = "affiliationName";
    private static final String INSTITUTION_ID = "institutionId";
    private static final String FACULTY_ID = "facultyId";
    private static final String DEPARTMENT_ID = "departmentId";
    private static final String GROUP_ID = "groupId";
    private static final String FUNDING_SOURCE = "fundingSource";
    private static final String FUNDING_ID = "fundingId";
    private static final String FUNDING_NAME = "fundingName";
    private static final String CHANNEL_TYPE = "channelType";
    private static final String CHANNEL_IDENTIFIER = "channelIdentifier";
    private static final String CHANNEL_NAME = "channelName";
    private static final String CHANNEL_ONLINE_ISSN = "channelOnlineIssn";
    private static final String CHANNEL_PRINT_ISSN = "channelPrintIssn";
    private static final String CHANNEL_LEVEL = "channelLevel";
    private static final String DELIMITER = ",";
    private static final String INSTITUTION_POINTS = "institutionPoints";
    private static final String INSTITUTION_APPROVAL_STATUS = "institutionApprovalStatus";
    private static final String PUBLICATION_DATE = "publicationDate";
    private static final List<String> AFFILIATION_HEADERS = List.of(PUBLICATION_ID, PUBLICATION_IDENTIFIER,
                                                                    CONTRIBUTOR_ID, CONTRIBUTOR_NAME, AFFILIATION_ID,
                                                                    AFFILIATION_NAME, INSTITUTION_ID, FACULTY_ID,
                                                                    DEPARTMENT_ID, GROUP_ID);
    private static final List<String> FUNDING_HEADERS = List.of(PUBLICATION_ID, PUBLICATION_IDENTIFIER, FUNDING_SOURCE,
                                                                FUNDING_ID, FUNDING_NAME);
    private static final List<String> PUBLICATION_HEADERS = List.of(PUBLICATION_ID, PUBLICATION_TITLE,
                                                                    PUBLICATION_CATEGORY, PUBLICATION_DATE,
                                                                    CHANNEL_TYPE, CHANNEL_IDENTIFIER, CHANNEL_NAME,
                                                                    CHANNEL_ONLINE_ISSN, CHANNEL_PRINT_ISSN,
                                                                    CHANNEL_LEVEL, PUBLICATION_IDENTIFIER);
    private static final List<String> CONTRIBUTOR_HEADERS = List.of(PUBLICATION_ID, PUBLICATION_IDENTIFIER,
                                                                    CONTRIBUTOR_IDENTIFIER, CONTRIBUTOR_NAME,
                                                                    CONTRIBUTOR_SEQUENCE_NUMBER, CONTRIBUTOR_ROLE);
    private static final List<String> IDENTIFIER_HEADERS = List.of(PUBLICATION_ID, PUBLICATION_IDENTIFIER,
                                                                   FUNDING_SOURCE, FUNDING_ID);
    private static final List<String> NVI_HEADERS = List.of(PUBLICATION_ID,
                                                            CONTRIBUTOR_IDENTIFIER,
                                                            AFFILIATION_ID, INSTITUTION_ID, INSTITUTION_POINTS,
                                                            INSTITUTION_APPROVAL_STATUS);
    private final List<TestPublication> publicationTestData;

    private final List<TestNviCandidate> nviTestData;
    private final Model model;

    public TestData(List<DatePair> dates) {
        this.model = ModelFactory.createDefaultModel();
        this.publicationTestData = generatePublicationData(dates);
        this.nviTestData = generateNviData(dates);
        addPublicationDataToModel(publicationTestData);
        addNviDataToModel(nviTestData);
    }

    public Model getModel() {
        return model;
    }

    public String getAffiliationResponseData() {
        var headers = String.join(DELIMITER, AFFILIATION_HEADERS) + CRLF.getString();
        publicationTestData.sort(this::sortByPublicationUri);
        var values = publicationTestData.stream()
                         .map(TestPublication::getExpectedAffiliationResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getContributorResponseData() {
        var headers = String.join(DELIMITER, CONTRIBUTOR_HEADERS) + CRLF.getString();
        publicationTestData.sort(this::sortByPublicationUri);
        var values = publicationTestData.stream()
                         .map(TestPublication::getExpectedContributorResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getFundingResponseData() {
        var headers = String.join(DELIMITER, FUNDING_HEADERS) + CRLF.getString();
        publicationTestData.sort(this::sortByPublicationUri);
        var values = publicationTestData.stream()
                         .map(TestPublication::getExpectedFundingResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getIdentifierResponseData() {
        var headers = String.join(DELIMITER, IDENTIFIER_HEADERS) + CRLF.getString();
        publicationTestData.sort(this::sortByPublicationUri);
        var values = publicationTestData.stream()
                         .map(TestPublication::getExpectedIdentifierResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getPublicationResponseData() {
        var headers = String.join(DELIMITER, PUBLICATION_HEADERS) + CRLF.getString();
        publicationTestData.sort(this::sortByPublicationUri);
        var values = publicationTestData.stream()
                         .map(TestPublication::getExpectedPublicationResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getNviResponseData() {
        var headers = String.join(DELIMITER, NVI_HEADERS) + CRLF.getString();
        nviTestData.sort(this::sortByPublicationUri);
        var values = nviTestData.stream()
                         .map(TestNviCandidate::getExpectedNviResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    private static TestPublication generatePublication(PublicationDate date, Instant modifiedDate) {
        var identifier = UUID.randomUUID();
        return new TestPublication()
                   .withModifiedDate(modifiedDate)
                   .withPublicationDate(date)
                   .withPublicationUri(Constants.publicationUri(identifier))
                   .withPublicationIdentifier(identifier.toString())
                   .withContributors(List.of(generateContributor()))
                   .withFundings(List.of(generateFunding()))
                   .withPublicationTitle("My study")
                   .withPublicationCategory("AcademicArticle")
                   .withPublicationDate(date)
                   .withChannel(generateChannel());
    }

    private static TestChannel generateChannel() {
        return new TestChannel()
                   .withType(Constants.ontologyUri("Journal"))
                   .withIdentifier(UUID.randomUUID())
                   .withName("Journal of Studies")
                   .withOnlineIssn("1111-1111")
                   .withPrintIssn("1212-122X")
                   .withScientificValue("LevelTwo");
    }

    private static TestFunding generateFunding() {
        return new TestFunding()
                   .withName("My big funding")
                   .withId("my-funding-1")
                   .withFundingSource(Constants.fundingSourceUri("my-funding"))
                   .withName("My-funding");
    }

    private static TestContributor generateContributor() {
        return new TestContributor()
                   .withIdentity(new TestIdentity(Constants.person("54431"), "Jim Person"))
                   .withContributorSequenceNo("1")
                   .withContributorRole("Creator")
                   .withAffiliations(List.of(generateAffiliation()));
    }

    private static TestOrganization generateAffiliation() {
        return new TestOrganization(Constants.organizationUri("10.1.1.2"), "My university");
    }

    private TestNviCandidate generateNviCandidate(Instant modifiedDate) {
        return TestNviCandidate.builder()
                   .withIdentifier(UUID.randomUUID().toString())
                   .withModifiedDate(modifiedDate)
                   .withPublicationDetails(generatePublicationDetails())
                   .build();
    }

    private TestPublicationDetails generatePublicationDetails() {
        return TestPublicationDetails.builder()
                   .withId(randomUri().toString())
                   .withContributors(List.of(generateNviContributor()))
                   .build();
    }

    private TestNviContributor generateNviContributor() {
        return TestNviContributor.builder()
                   .withId(randomUri().toString())
                   .withIsNviContributor(true)
                   .withAffiliations(List.of(generateNviAffiliation()))
                   .build();
    }

    private TestAffiliation generateNviAffiliation() {
        return TestAffiliation.builder()
                   .withId(randomUri().toString())
                   .withIsNviAffiliation(true)
                   .build();
    }

    private void addPublicationDataToModel(List<TestPublication> testData) {
        testData.stream()
            .map(TestPublication::generateModel)
            .forEach(model::add);
    }

    private void addNviDataToModel(List<TestNviCandidate> testData) {
        testData.stream()
            .map(TestNviCandidate::generateModel)
            .forEach(model::add);
    }

    private List<TestPublication> generatePublicationData(List<DatePair> dates) {
        var dataSet = new ArrayList<TestPublication>();
        for (DatePair date : dates) {
            var data = generatePublication(date.publicationDate, date.modifiedDate);
            dataSet.add(data);
        }
        return dataSet;
    }

    private List<TestNviCandidate> generateNviData(List<DatePair> dates) {
        var dataSet = new ArrayList<TestNviCandidate>();
        for (DatePair date : dates) {
            var data = generateNviCandidate(date.modifiedDate);
            dataSet.add(data);
        }
        return dataSet;
    }

    private int sortByPublicationUri(TestPublication a, TestPublication b) {
        return a.getPublicationUri().compareTo(b.getPublicationUri());
    }

    private int sortByPublicationUri(TestNviCandidate a, TestNviCandidate b) {
        return a.publicationDetails().id().compareTo(b.publicationDetails().id());
    }

    public record DatePair(PublicationDate publicationDate, Instant modifiedDate) {

    }
}

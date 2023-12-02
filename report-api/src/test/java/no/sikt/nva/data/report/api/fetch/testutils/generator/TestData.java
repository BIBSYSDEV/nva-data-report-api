package no.sikt.nva.data.report.api.fetch.testutils.generator;

import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class TestData {

    public static final String PUBLICATION_ID = "publicationId";
    public static final String PUBLICATION_IDENTIFIER = "publicationIdentifier";
    public static final String PUBLICATION_CATEGORY = "publicationCategory";
    public static final String PUBLICATION_TITLE = "publicationTitle";
    public static final String CONTRIBUTOR_SEQUENCE_NUMBER = "contributorSequenceNumber";
    public static final String CONTRIBUTOR_ROLE = "contributorRole";
    public static final String CONTRIBUTOR_ID = "contributorId";
    public static final String CONTRIBUTOR_IDENTIFIER = "contributorIdentifier";
    public static final String CONTRIBUTOR_NAME = "contributorName";
    public static final String AFFILIATION_ID = "affiliationId";
    public static final String AFFILIATION_NAME = "affiliationName";
    public static final String INSTITUTION_ID = "institutionId";
    public static final String FACULTY_ID = "facultyId";
    public static final String DEPARTMENT_ID = "departmentId";
    public static final String GROUP_ID = "groupId";
    public static final String FUNDING_SOURCE = "fundingSource";
    public static final String FUNDING_IDENTIFIER = "fundingIdentifier";
    public static final String FUNDING_NAME = "fundingName";
    public static final String CHANNEL_TYPE = "channelType";
    public static final String CHANNEL_IDENTIFIER = "channelIdentifier";
    public static final String CHANNEL_NAME = "channelName";
    public static final String CHANNEL_ONLINE_ISSN = "channelOnlineIssn";
    public static final String CHANNEL_PRINT_ISSN = "channelPrintIssn";
    public static final String CHANNEL_LEVEL = "channelLevel";
    public static final String DELIMITER = ",";
    public static final List<String> AFFILIATION_HEADERS = List.of(PUBLICATION_ID, PUBLICATION_IDENTIFIER,
                                                                   CONTRIBUTOR_ID, CONTRIBUTOR_NAME, AFFILIATION_ID,
                                                                   AFFILIATION_NAME, INSTITUTION_ID, FACULTY_ID,
                                                                   DEPARTMENT_ID, GROUP_ID);
    private static final List<String> CONTRIBUTOR_HEADERS = List.of(PUBLICATION_ID, PUBLICATION_IDENTIFIER,
                                                                    CONTRIBUTOR_IDENTIFIER, CONTRIBUTOR_NAME,
                                                                    CONTRIBUTOR_SEQUENCE_NUMBER,CONTRIBUTOR_ROLE);

    private final List<TestPublication> testData;
    private final Model model;

    public TestData(List<DatePair> dates) {
        this.model = ModelFactory.createDefaultModel();
        this.testData = generateData(dates);
        generateModel(testData);
    }

    public Model getModel() {
        return model;
    }


    public String getAffiliationResponseData() {
        var headers = String.join(DELIMITER, AFFILIATION_HEADERS) + CRLF.getString();
        testData.sort(this::sortByPublicationUri);
        var values = testData.stream()
                         .map(TestPublication::getExpectedAffiliationResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }
    
    public String getContributorResponseData() {
        var headers = String.join(DELIMITER, CONTRIBUTOR_HEADERS) + CRLF.getString();
        testData.sort(this::sortByPublicationUri);
        var values = testData.stream()
                         .map(TestPublication::getExpectedContributorResponse)
                         .collect(Collectors.joining());
        return headers + values;    }

    private void generateModel(List<TestPublication> testData) {
        testData.stream()
            .map(TestPublication::generateModel)
            .forEach(model::add);
    }

    private List<TestPublication> generateData(List<DatePair> dates) {
        var dataSet = new ArrayList<TestPublication>();
        for (DatePair date : dates) {

            var data = generatePublication(date.publicationDate, date.modifiedDate);
            dataSet.add(data);
        }
        return dataSet;
    }

    private static TestPublication generatePublication(PublicationDate date, Instant modifiedDate) {
        UUID identifier = UUID.randomUUID();
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
                   .withFundingName("My big funding")
                   .withFundingIdentifier("my-funding-1")
                   .withFundingSource(Constants.fundingSourceUri("my-funding"))
                   .withFundingName("My-funding");
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

    private int sortByPublicationUri(TestPublication a, TestPublication b) {
        return a.getPublicationUri().compareTo(b.getPublicationUri());
    }

    public record DatePair(PublicationDate publicationDate, Instant modifiedDate) {

    }
}

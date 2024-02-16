package no.sikt.nva.data.report.api.fetch.testutils.generator;

import static no.sikt.nva.data.report.api.fetch.testutils.generator.Constants.organizationUri;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestApproval;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestApproval.ApprovalStatus;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviCandidate;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviContributor;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviOrganization;
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

    public static final String PUBLICATION_ID = "publicationId";
    public static final String CONTRIBUTOR_IDENTIFIER = "contributorIdentifier";
    public static final String SOME_TOP_LEVEL_IDENTIFIER = "10.0.0.0";
    private static final String PUBLICATION_IDENTIFIER = "publicationIdentifier";
    private static final String PUBLICATION_CATEGORY = "publicationCategory";
    private static final String PUBLICATION_TITLE = "publicationTitle";
    private static final String CONTRIBUTOR_SEQUENCE_NUMBER = "contributorSequenceNumber";
    private static final String CONTRIBUTOR_ROLE = "contributorRole";
    private static final String CONTRIBUTOR_ID = "contributorId";
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
    private static final BigDecimal MIN_BIG_DECIMAL = BigDecimal.ZERO;
    private static final BigDecimal MAX_BIG_DECIMAL = BigDecimal.TEN;
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
    private static final String SOME_SUB_UNIT_IDENTIFIER = "10.1.1.2";
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

    public static BigDecimal randomBigDecimal() {
        var randomBigDecimal = MIN_BIG_DECIMAL.add(
            BigDecimal.valueOf(Math.random()).multiply(MAX_BIG_DECIMAL.subtract(MIN_BIG_DECIMAL)));
        return randomBigDecimal.setScale(4, RoundingMode.HALF_UP);
    }

    public Model getModel() {
        return model;
    }

    public String getAffiliationResponseData(int offset, int pageSize, List<String> databaseOrderedPublicationUris) {
        var headers = String.join(DELIMITER, AFFILIATION_HEADERS) + CRLF.getString();
        var sorted = sortByDatabaseOrder(publicationTestData, databaseOrderedPublicationUris);
        var values = sorted.stream()
                         .skip(offset)
                         .limit(pageSize)
                         .map(TestPublication::getExpectedAffiliationResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getContributorResponseData(int offset, int pageSize, List<String> databaseOrderedPublicationUris) {
        var headers = String.join(DELIMITER, CONTRIBUTOR_HEADERS) + CRLF.getString();
        var sorted = sortByDatabaseOrder(publicationTestData, databaseOrderedPublicationUris);
        var values = sorted.stream()
                         .skip(offset)
                         .limit(pageSize)
                         .map(TestPublication::getExpectedContributorResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getFundingResponseData(int offset, int pageSize, List<String> databaseOrderedPublicationUris) {
        var headers = String.join(DELIMITER, FUNDING_HEADERS) + CRLF.getString();
        var sorted = sortByDatabaseOrder(publicationTestData, databaseOrderedPublicationUris);
        var values = sorted.stream()
                         .skip(offset)
                         .limit(pageSize)
                         .map(TestPublication::getExpectedFundingResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getIdentifierResponseData(int offset, int pageSize, List<String> databaseOrderedPublicationUris) {
        var headers = String.join(DELIMITER, IDENTIFIER_HEADERS) + CRLF.getString();
        var sorted = sortByDatabaseOrder(publicationTestData, databaseOrderedPublicationUris);
        var values = sorted.stream()
                         .skip(offset)
                         .limit(pageSize)
                         .map(TestPublication::getExpectedIdentifierResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getPublicationResponseData(int offset, int pageSize, List<String> databaseOrderedPublicationUris) {
        var headers = String.join(DELIMITER, PUBLICATION_HEADERS) + CRLF.getString();
        var sorted = sortByDatabaseOrder(publicationTestData, databaseOrderedPublicationUris);
        var values = sorted.stream()
                         .skip(offset)
                         .limit(pageSize)
                         .map(TestPublication::getExpectedPublicationResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getNviResponseData(int offset, int pageSize,
                                     Map<String, List<String>> databaseOrderedResults) {
        var headers = String.join(DELIMITER, NVI_HEADERS) + CRLF.getString();
        var sorted = sortCandidatesByDatabaseOrder(nviTestData, databaseOrderedResults.keySet().stream().toList());
        sorted.forEach(testNviCandidate -> {
            var databaseOrderedContributors = databaseOrderedResults.get(testNviCandidate.publicationDetails().id());
            testNviCandidate.sortContributorsByDatabaseOrder(databaseOrderedContributors);
        });
        var values = sorted.stream()
                         .skip(offset)
                         .limit(pageSize)
                         .map(testNviCandidate -> testNviCandidate.getExpectedNviResponse(offset, pageSize))
                         .collect(Collectors.joining());
        return headers + values;
    }

    private static List<TestPublication> sortByDatabaseOrder(List<TestPublication> testPublications,
                                                             List<String> databaseOrderedPublicationUris) {
        var indexMap = getStringIntegerHashMap(databaseOrderedPublicationUris);
        return testPublications.stream()
                   .sorted(Comparator.comparingInt(publication -> indexMap.get(publication.getPublicationUri())))
                   .collect(Collectors.toList());
    }

    private static HashMap<String, Integer> getStringIntegerHashMap(List<String> databaseOrderedPublicationUris) {
        var indexMap = new HashMap<String, Integer>();
        for (int i = 0; i < databaseOrderedPublicationUris.size(); i++) {
            indexMap.put(databaseOrderedPublicationUris.get(i), i);
        }
        return indexMap;
    }

    private static List<TestNviCandidate> sortCandidatesByDatabaseOrder(List<TestNviCandidate> testCandidates,
                                                                        List<String> databaseOrderedPublicationUris) {
        var indexMap = getStringIntegerHashMap(databaseOrderedPublicationUris);
        return testCandidates.stream()
                   .sorted(Comparator.comparingInt(candidate -> indexMap.get(candidate.publicationDetails().id())))
                   .collect(Collectors.toList());
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
        return new TestOrganization(organizationUri(SOME_SUB_UNIT_IDENTIFIER), "My university");
    }

    private static List<TestApproval> generateApprovals(TestPublicationDetails publicationDetails) {
        return publicationDetails.contributors().stream()
                   .flatMap(contributor -> contributor.affiliations().stream())
                   .map(TestNviOrganization::getTopLevelOrganization)
                   .distinct()
                   .map(TestData::generateApproval)
                   .toList();
    }

    private static TestApproval generateApproval(String topLevelOrganization) {
        return TestApproval.builder()
                   .withInstitutionId(URI.create(topLevelOrganization))
                   .withApprovalStatus(randomElement(ApprovalStatus.values()))
                   .withPoints(randomBigDecimal())
                   .build();
    }

    private TestNviCandidate generateNviCandidate(Instant modifiedDate) {
        var publicationDetails = generatePublicationDetails();
        var approvals = generateApprovals(publicationDetails);
        return TestNviCandidate.builder()
                   .withIdentifier(UUID.randomUUID().toString())
                   .withModifiedDate(modifiedDate)
                   .withPublicationDetails(publicationDetails)
                   .withApprovals(approvals)
                   .build();
    }

    private TestPublicationDetails generatePublicationDetails() {
        return TestPublicationDetails.builder()
                   .withId(randomUri().toString())
                   .withContributors(new ArrayList<>(List.of(generateNviContributor(SOME_SUB_UNIT_IDENTIFIER),
                                                             generateNviContributor(SOME_TOP_LEVEL_IDENTIFIER))))
                   .build();
    }

    private TestNviContributor generateNviContributor(String organizationIdentifier) {
        return TestNviContributor.builder()
                   .withId(randomUri().toString())
                   .withAffiliations(List.of(generateNviAffiliation(organizationIdentifier)))
                   .build();
    }

    private TestNviOrganization generateNviAffiliation(String organizationIdentifier) {
        return TestNviOrganization.builder()
                   .withId(organizationUri(organizationIdentifier))
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
    public record DatePair(PublicationDate publicationDate, Instant modifiedDate) {

    }
}

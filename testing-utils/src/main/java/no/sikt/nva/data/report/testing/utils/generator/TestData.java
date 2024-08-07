package no.sikt.nva.data.report.testing.utils.generator;

import static no.sikt.nva.data.report.testing.utils.generator.Constants.organizationUri;
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
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_CATEGORY;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_DATE;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_ID;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_TITLE;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.STATUS;
import static org.apache.commons.io.StandardLineSeparator.CRLF;
import commons.ViewCompiler;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviCandidate;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviContributor;
import no.sikt.nva.data.report.testing.utils.generator.publication.PublicationDate;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestChannel;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestContributor;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestFunding;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestIdentity;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestOrganization;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestPublication;
import org.apache.jena.rdf.model.Model;

public class TestData {

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
                                                                    DEPARTMENT_ID, GROUP_ID);
    private static final List<String> FUNDING_HEADERS = List.of(PUBLICATION_ID, STATUS,
                                                                PUBLICATION_IDENTIFIER,
                                                                FUNDING_SOURCE,
                                                                FUNDING_ID, FUNDING_NAME);
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
                                                                    PUBLICATION_IDENTIFIER);
    private static final List<String> CONTRIBUTOR_HEADERS = List.of(PUBLICATION_ID, STATUS,
                                                                    PUBLICATION_IDENTIFIER,
                                                                    CONTRIBUTOR_IDENTIFIER,
                                                                    CONTRIBUTOR_NAME,
                                                                    CONTRIBUTOR_SEQUENCE_NUMBER,
                                                                    CONTRIBUTOR_ROLE);
    private static final List<String> IDENTIFIER_HEADERS = List.of(PUBLICATION_ID, STATUS,
                                                                   PUBLICATION_IDENTIFIER,
                                                                   FUNDING_SOURCE, FUNDING_ID);
    private final List<TestPublication> publicationTestData;
    private final List<TestNviCandidate> nviTestData;
    private final List<Model> models;

    public TestData(List<DatePair> dates) {
        this.models = new ArrayList<>();
        this.publicationTestData = generatePublicationData(dates);
        this.nviTestData = NviTestData.generateNviData(publicationTestData);
        addPublicationDataToModel(publicationTestData);
        addNviDataToModel(nviTestData);
    }

    public List<TestPublication> getPublicationTestData() {
        return publicationTestData;
    }

    public List<Model> getModels() {
        return models;
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
        var headers = String.join(DELIMITER, NviTestData.NVI_HEADERS) + CRLF.getString();
        nviTestData.sort(this::sortByPublicationUri);
        sortContributors(nviTestData);
        var values = nviTestData.stream()
                         .map(TestNviCandidate::getExpectedNviResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    public String getNviInstitutionStatusResponseData(String reportingYear, URI institutionId) {
        var headers = String.join(DELIMITER, NviInstitutionStatusTestData.NVI_INSTITUTION_STATUS_HEADERS)
                      + CRLF.getString();
        var expectedCandidates = getExpectedCandidates(reportingYear, institutionId);
        sortContributors(expectedCandidates);
        var values = expectedCandidates.stream()
                         .map(this::getExpectedNviInstitutionStatusResponse)
                         .collect(Collectors.joining());
        return headers + values;
    }

    private static boolean isReportedInYear(String reportingYear, TestNviCandidate testNviCandidate) {
        return testNviCandidate.reportingPeriod().equals(reportingYear);
    }

    private static boolean hasAnyApprovals(URI institutionId, TestNviCandidate testNviCandidate) {
        return testNviCandidate.approvals()
                   .stream()
                   .anyMatch(approval -> approval.institutionId().equals(institutionId));
    }

    private static TestPublication generatePublication(PublicationDate date, Instant modifiedDate) {
        var identifier = UUID.randomUUID();
        return new TestPublication()
                   .withModifiedDate(modifiedDate)
                   .withPublicationStatus("PUBLISHED")
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

    private static boolean hasSamePublicationId(TestNviCandidate candidate, TestPublication publication) {
        return publication.getPublicationUri().equals(candidate.publicationDetails().id());
    }

    private static Model getModelWithAppliedView(TestPublication publication) {
        var id = URI.create(publication.getPublicationUri());
        var model = publication.generateModel();
        return new ViewCompiler(model).extractView(id);
    }

    private static Model getModelWithAppliedView(TestNviCandidate candidate) {
        var id = URI.create(candidate.candidateUri());
        var model = candidate.generateModel();
        return new ViewCompiler(model).extractView(id);
    }

    private void sortContributors(List<TestNviCandidate> expectedCandidates) {
        expectedCandidates.forEach(candidate -> candidate.publicationDetails()
                                                    .contributors()
                                                    .sort(this::sortByContributor));
    }

    private List<TestNviCandidate> getExpectedCandidates(String reportingYear, URI institutionId) {
        return nviTestData.stream()
                   .filter(TestNviCandidate::isApplicable)
                   .filter(candidate -> isReportedInYear(reportingYear, candidate))
                   .filter(candidate -> hasAnyApprovals(institutionId, candidate))
                   .sorted(this::sortByPublicationUri)
                   .toList();
    }

    private String getExpectedNviInstitutionStatusResponse(TestNviCandidate expectedCandidate) {
        return expectedCandidate.publicationDetails()
                   .contributors()
                   .stream()
                   .map(contributor -> NviInstitutionStatusTestData.generateExpectedNviInstitutionResponse(contributor,
                                                                                                           expectedCandidate,
                                                                                                           getPublication(
                                                                                                               expectedCandidate)))
                   .collect(Collectors.joining());
    }

    private TestPublication getPublication(TestNviCandidate expectedCandidate) {
        return publicationTestData.stream()
                   .filter(publication -> hasSamePublicationId(expectedCandidate, publication))
                   .findFirst()
                   .orElseThrow();
    }

    private void addPublicationDataToModel(List<TestPublication> testData) {
        testData.stream()
            .map(TestData::getModelWithAppliedView)
            .forEach(models::add);
    }

    private void addNviDataToModel(List<TestNviCandidate> testData) {
        testData.stream()
            .map(TestData::getModelWithAppliedView)
            .forEach(models::add);
    }

    private List<TestPublication> generatePublicationData(List<DatePair> dates) {
        var dataSet = new ArrayList<TestPublication>();
        for (DatePair date : dates) {
            var data = generatePublication(date.publicationDate, date.modifiedDate);
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

    private int sortByContributor(TestNviContributor a, TestNviContributor b) {
        return a.id().compareTo(b.id());
    }

    public record DatePair(PublicationDate publicationDate, Instant modifiedDate) {

    }
}

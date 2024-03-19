package no.sikt.nva.data.report.api.fetch.testutils.generator;

import static no.sikt.nva.data.report.api.fetch.FetchNviInstitutionReportTest.SOME_YEAR;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.Constants.organizationUri;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviHeaders.AUTHOR_SHARE_COUNT;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviHeaders.GLOBAL_APPROVAL_STATUS;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviHeaders.INSTITUTION_APPROVAL_STATUS;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviHeaders.INSTITUTION_POINTS;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviHeaders.INTERNATIONAL_COLLABORATION_FACTOR;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviHeaders.IS_APPLICABLE;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviHeaders.POINTS_FOR_AFFILIATION;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviHeaders.PUBLICATION_TYPE_CHANNEL_LEVEL_POINTS;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviHeaders.REPORTED_PERIOD;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviHeaders.TOTAL_POINTS;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.PublicationHeaders.AFFILIATION_ID;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.PublicationHeaders.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.PublicationHeaders.INSTITUTION_ID;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.PublicationHeaders.PUBLICATION_ID;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.TestData.SOME_SUB_UNIT_IDENTIFIER;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.TestData.SOME_TOP_LEVEL_IDENTIFIER;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import no.sikt.nva.data.report.api.fetch.testutils.generator.TestData.DatePair;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestApproval;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestApprovalStatus;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviCandidate;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviCandidate.Builder;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviContributor;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviOrganization;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestPublicationDetails;
import no.sikt.nva.data.report.api.fetch.testutils.generator.publication.TestPublication;

public final class NviTestData {

    public static final List<String> NVI_HEADERS = List.of(PUBLICATION_ID,
                                                           CONTRIBUTOR_IDENTIFIER,
                                                           AFFILIATION_ID, INSTITUTION_ID,
                                                           INSTITUTION_POINTS,
                                                           POINTS_FOR_AFFILIATION,
                                                           INSTITUTION_APPROVAL_STATUS,
                                                           GLOBAL_APPROVAL_STATUS,
                                                           REPORTED_PERIOD,
                                                           TOTAL_POINTS,
                                                           PUBLICATION_TYPE_CHANNEL_LEVEL_POINTS,
                                                           AUTHOR_SHARE_COUNT,
                                                           INTERNATIONAL_COLLABORATION_FACTOR,
                                                           IS_APPLICABLE);

    private static final BigDecimal MIN_BIG_DECIMAL = BigDecimal.ZERO;
    private static final BigDecimal MAX_BIG_DECIMAL = BigDecimal.TEN;

    private NviTestData() {
        // NO-OP
    }

    public static BigDecimal randomBigDecimal() {
        var randomBigDecimal = MIN_BIG_DECIMAL.add(
            BigDecimal.valueOf(Math.random()).multiply(MAX_BIG_DECIMAL.subtract(MIN_BIG_DECIMAL)));
        return randomBigDecimal.setScale(4, RoundingMode.HALF_UP);
    }

    static List<TestNviCandidate> generateNviData(List<DatePair> dates, List<TestPublication> publications) {
        var dataSet = new ArrayList<TestNviCandidate>();
        addCandidatesForPublications(publications, dates.getFirst().modifiedDate(), dataSet);
        for (var date : dates) {
            var modifiedDate = date.modifiedDate();
            var reportingYear = date.publicationDate().year();
            addNonApplicableNviCandidate(modifiedDate, reportingYear, dataSet);
            addReportedCandidate(modifiedDate, reportingYear, dataSet);
            addCoPublishedCandidate(modifiedDate, reportingYear, dataSet);
        }
        return dataSet;
    }

    private static void addCoPublishedCandidate(Instant modifiedDate, String reportingYear,
                                                ArrayList<TestNviCandidate> dataSet) {
        var coPublishedCandidate = generateCoPublishedNviCandidate(modifiedDate, reportingYear);
        dataSet.add(coPublishedCandidate);
    }

    private static void addReportedCandidate(Instant modifiedDate, String reportingYear,
                                             ArrayList<TestNviCandidate> dataSet) {
        var reportedCandidate = generateReportedNviCandidate(modifiedDate, reportingYear);
        dataSet.add(reportedCandidate);
    }

    private static void addNonApplicableNviCandidate(Instant modifiedDate, String reportingYear,
                                                     ArrayList<TestNviCandidate> dataSet) {
        var nonApplicableNviCandidate = generateNonApplicableNviCandidate(modifiedDate, reportingYear);
        dataSet.add(nonApplicableNviCandidate);
    }

    private static void addCandidatesForPublications(List<TestPublication> publications, Instant modifiedDate,
                                                     ArrayList<TestNviCandidate> dataSet) {
        for (TestPublication publication : publications) {
            var nviCandidate = generateNviCandidate(modifiedDate,
                                                    publication.getPublicationUri());
            dataSet.add(nviCandidate);
        }
    }

    private static TestNviCandidate generateNviCandidate(Instant modifiedDate,
                                                         String publicationId) {
        var publicationDetails = generatePublicationDetails(publicationId);
        var approvals = generateApprovals(publicationDetails);
        return getCandidateBuilder(true, modifiedDate, publicationDetails, approvals, SOME_YEAR).build();
    }

    private static TestNviCandidate generateReportedNviCandidate(Instant modifiedDate, String reportingYear) {
        var publicationDetails = generatePublicationDetails();
        var approvals = generateApprovals(publicationDetails);
        return getCandidateBuilder(true, modifiedDate, publicationDetails, approvals, reportingYear)
                   .withReported(true)
                   .build();
    }

    private static TestNviCandidate generateCoPublishedNviCandidate(Instant modifiedDate, String reportingYear) {
        var publicationDetails = TestPublicationDetails.builder()
                                     .withId(randomUri().toString())
                                     .withContributors(
                                         new ArrayList<>(List.of(generateNviContributor(SOME_TOP_LEVEL_IDENTIFIER),
                                                                 generateNviContributor("90.0.0.0"))))
                                     .build();
        var approvals = generateApprovals(publicationDetails);
        return getCandidateBuilder(true, modifiedDate, publicationDetails, approvals, reportingYear).build();
    }

    @SuppressWarnings("unchecked")
    private static TestNviCandidate generateNonApplicableNviCandidate(Instant modifiedDate, String reportingYear) {
        return getCandidateBuilder(false, modifiedDate, generatePublicationDetails(),
                                   Collections.EMPTY_LIST,
                                   reportingYear).build();
    }

    private static TestPublicationDetails generatePublicationDetails() {
        return generatePublicationDetails(randomUri().toString());
    }

    private static TestPublicationDetails generatePublicationDetails(String publicationId) {
        return TestPublicationDetails.builder()
                   .withId(publicationId)
                   .withContributors(new ArrayList<>(List.of(generateNviContributor(SOME_SUB_UNIT_IDENTIFIER),
                                                             generateNviContributor(SOME_TOP_LEVEL_IDENTIFIER))))
                   .build();
    }

    private static TestNviContributor generateNviContributor(String organizationIdentifier) {
        return TestNviContributor.builder()
                   .withId(randomUri().toString())
                   .withAffiliations(List.of(generateNviAffiliation(organizationIdentifier)))
                   .build();
    }

    private static TestNviOrganization generateNviAffiliation(String organizationIdentifier) {
        return TestNviOrganization.builder()
                   .withId(organizationUri(organizationIdentifier))
                   .build();
    }

    private static List<TestApproval> generateApprovals(TestPublicationDetails publicationDetails) {
        return publicationDetails.contributors().stream()
                   .flatMap(contributor -> contributor.affiliations().stream())
                   .map(TestNviOrganization::getTopLevelOrganization)
                   .distinct()
                   .map(NviTestData::generateApproval)
                   .toList();
    }

    private static TestApproval generateApproval(String topLevelOrganization) {
        return TestApproval.builder()
                   .withInstitutionId(URI.create(topLevelOrganization))
                   .withApprovalStatus(randomElement(TestApprovalStatus.values()))
                   .withPoints(randomBigDecimal())
                   .build();
    }

    private static Builder getCandidateBuilder(boolean isApplicable, Instant modifiedDate,
                                               TestPublicationDetails publicationDetails,
                                               List<TestApproval> approvals, String reportingPeriod) {
        return TestNviCandidate.builder()
                   .withIsApplicable(isApplicable)
                   .withIdentifier(UUID.randomUUID().toString())
                   .withModifiedDate(modifiedDate)
                   .withPublicationDetails(publicationDetails)
                   .withApprovals(approvals)
                   .withCreatorShareCount(countCombinationsOfCreatorsAndAffiliations(publicationDetails))
                   .withInternationalCollaborationFactor(BigDecimal.ONE)
                   .withGlobalApprovalStatus(TestApprovalStatus.PENDING)
                   .withPublicationTypeChannelLevelPoints(randomBigDecimal())
                   .withTotalPoints(randomBigDecimal())
                   .withReportingPeriod(reportingPeriod);
    }

    private static int countCombinationsOfCreatorsAndAffiliations(TestPublicationDetails publicationDetails) {
        return publicationDetails.contributors()
                   .stream()
                   .flatMap(contributor -> contributor.affiliations().stream())
                   .toList()
                   .size();
    }
}

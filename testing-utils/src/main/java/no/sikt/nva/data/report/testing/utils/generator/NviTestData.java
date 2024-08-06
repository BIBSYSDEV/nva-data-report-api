package no.sikt.nva.data.report.testing.utils.generator;

import static no.sikt.nva.data.report.testing.utils.generator.Constants.organizationUri;
import static no.sikt.nva.data.report.testing.utils.generator.TestData.SOME_SUB_UNIT_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.TestData.SOME_TOP_LEVEL_IDENTIFIER;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestApproval;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestApprovalStatus;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestCreatorAffiliationPoints;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestGlobalApprovalStatus;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestInstitutionPoints;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviCandidate;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviCandidate.Builder;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviContributor;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestNviOrganization;
import no.sikt.nva.data.report.testing.utils.generator.nvi.TestPublicationDetails;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestPublication;
import no.unit.nva.testutils.RandomDataGenerator;

public final class NviTestData {

    public static final List<String> NVI_HEADERS = List.of(PublicationHeaders.PUBLICATION_ID,
                                                           PublicationHeaders.CONTRIBUTOR_IDENTIFIER,
                                                           PublicationHeaders.AFFILIATION_ID, PublicationHeaders.INSTITUTION_ID,
                                                           NviHeaders.INSTITUTION_POINTS,
                                                           NviHeaders.POINTS_FOR_AFFILIATION,
                                                           NviHeaders.INSTITUTION_APPROVAL_STATUS,
                                                           NviHeaders.GLOBAL_APPROVAL_STATUS,
                                                           NviHeaders.REPORTED_PERIOD,
                                                           NviHeaders.TOTAL_POINTS,
                                                           NviHeaders.PUBLICATION_TYPE_CHANNEL_LEVEL_POINTS,
                                                           NviHeaders.AUTHOR_SHARE_COUNT,
                                                           NviHeaders.INTERNATIONAL_COLLABORATION_FACTOR,
                                                           NviHeaders.IS_APPLICABLE);

    private static final BigDecimal MIN_BIG_DECIMAL = BigDecimal.ZERO;
    private static final BigDecimal MAX_BIG_DECIMAL = BigDecimal.TEN;
    public static final String SOME_YEAR = "2023";

    private NviTestData() {
        // NO-OP
    }

    public static BigDecimal randomBigDecimal() {
        var randomBigDecimal = MIN_BIG_DECIMAL.add(
            BigDecimal.valueOf(Math.random()).multiply(MAX_BIG_DECIMAL.subtract(MIN_BIG_DECIMAL)));
        return randomBigDecimal.setScale(4, RoundingMode.HALF_UP);
    }

    static List<TestNviCandidate> generateNviData(List<TestPublication> publications) {
        var dataSet = new ArrayList<TestNviCandidate>();
        for (TestPublication publication : publications) {
            var modifiedDate = publication.getModifiedDate();
            addApplicableNviCandidate(publication, modifiedDate, dataSet);
            var reportingYear = publication.getDate().year();
            addNonApplicableNviCandidate(modifiedDate, reportingYear, dataSet);
            addReportedCandidate(modifiedDate, reportingYear, dataSet);
            addCoPublishedCandidate(modifiedDate, reportingYear, dataSet);
        }
        return dataSet;
    }

    private static void addApplicableNviCandidate(TestPublication publication, Instant modifiedDate,
                                                  ArrayList<TestNviCandidate> dataSet) {
        var nviCandidate = generateNviCandidate(modifiedDate, publication);
        dataSet.add(nviCandidate);
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

    private static TestNviCandidate generateNviCandidate(Instant modifiedDate, TestPublication publication) {
        var publicationDetails = generatePublicationDetails(publication);
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
                                         new ArrayList<>(List.of(
                                             generateNviContributor(randomUri().toString(), SOME_TOP_LEVEL_IDENTIFIER),
                                             generateNviContributor(randomUri().toString(), "90.0.0.0"))))
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

    private static TestPublicationDetails generatePublicationDetails(TestPublication publication) {
        var contributors = publication.getContributors().stream()
                               .map(contributor -> generateNviContributor(contributor.getIdentity().uri(),
                                                                          SOME_SUB_UNIT_IDENTIFIER))
                               .toList();
        return TestPublicationDetails.builder()
                   .withId(publication.getPublicationUri())
                   .withContributors(new ArrayList<>(contributors))
                   .build();
    }

    private static TestPublicationDetails generatePublicationDetails(String publicationId) {
        return TestPublicationDetails.builder()
                   .withId(publicationId)
                   .withContributors(new ArrayList<>(List.of(
                       generateNviContributor(randomUri().toString(), SOME_SUB_UNIT_IDENTIFIER),
                       generateNviContributor(randomUri().toString(), SOME_TOP_LEVEL_IDENTIFIER))))
                   .build();
    }

    private static TestNviContributor generateNviContributor(String id, String organizationIdentifier) {
        return TestNviContributor.builder()
                   .withId(id)
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
                   .map(topLevelOrganization -> generateApproval(topLevelOrganization, publicationDetails))
                   .toList();
    }

    private static TestApproval generateApproval(String topLevelOrganization,
                                                 TestPublicationDetails publicationDetails) {
        var nviContributors = publicationDetails.filterContributorsWithTopLevelOrg(topLevelOrganization);
        var involvedOrgs = getAffiliations(topLevelOrganization, nviContributors);
        involvedOrgs.add(topLevelOrganization);
        return TestApproval.builder()
                   .withInstitutionId(URI.create(topLevelOrganization))
                   .withApprovalStatus(randomElement(TestApprovalStatus.values()))
                   .withPoints(generateInstitutionPoints(topLevelOrganization, nviContributors))
                   .withInvolvedOrganizations(involvedOrgs)
                   .build();
    }

    private static HashSet<String> getAffiliations(String topLevelOrganization,
                                                   List<TestNviContributor> nviContributors) {
        return nviContributors.stream()
                   .flatMap(contributor -> contributor.filterAffiliationsWithTopLevelOrg(topLevelOrganization).stream())
                   .map(TestNviOrganization::id)
                   .collect(Collectors.toCollection(HashSet::new));
    }

    private static TestInstitutionPoints generateInstitutionPoints(String topLevelOrganization,
                                                                   List<TestNviContributor> nviContributors) {
        return TestInstitutionPoints.builder()
                   .withCreatorAffiliationPoints(generateCreatorAffiliationPointsList(topLevelOrganization,
                                                                                      nviContributors))
                   .withInstitutionPoints(randomBigDecimal())
                   .build();
    }

    private static List<TestCreatorAffiliationPoints> generateCreatorAffiliationPointsList(
        String topLevelOrganization,
        List<TestNviContributor> nviContributors) {
        return nviContributors.stream()
                   .map(creator -> getCreatorAffiliationPoints(topLevelOrganization, creator))
                   .flatMap(List::stream)
                   .toList();
    }

    private static List<TestCreatorAffiliationPoints> getCreatorAffiliationPoints(String topLevelOrganization,
                                                                                  TestNviContributor creator) {
        return creator.filterAffiliationsWithTopLevelOrg(topLevelOrganization).stream()
                   .map(affiliation -> generateCreatorAffiliationPoints(creator, affiliation))
                   .toList();
    }

    private static TestCreatorAffiliationPoints generateCreatorAffiliationPoints(TestNviContributor creator,
                                                                                 TestNviOrganization affiliation) {
        return TestCreatorAffiliationPoints.builder()
                   .withNviCreator(URI.create(creator.id()))
                   .withAffiliationId(URI.create(affiliation.id()))
                   .withPoints(randomBigDecimal())
                   .build();
    }

    private static Builder getCandidateBuilder(boolean isApplicable, Instant modifiedDate,
                                               TestPublicationDetails publicationDetails,
                                               List<TestApproval> approvals, String reportingPeriod) {
        var identifier = UUID.randomUUID();
        return TestNviCandidate.builder()
                   .withIsApplicable(isApplicable)
                   .withIdentifier(identifier.toString())
                   .withCandidateUri(Constants.candidateUri(identifier))
                   .withModifiedDate(modifiedDate)
                   .withPublicationDetails(publicationDetails)
                   .withApprovals(approvals)
                   .withCreatorShareCount(countCombinationsOfCreatorsAndAffiliations(publicationDetails))
                   .withInternationalCollaborationFactor(randomBigDecimal())
                   .withGlobalApprovalStatus(RandomDataGenerator.randomElement(TestGlobalApprovalStatus.values()))
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
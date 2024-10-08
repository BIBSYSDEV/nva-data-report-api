package no.sikt.nva.data.report.testing.utils.generator;

import static no.sikt.nva.data.report.testing.utils.generator.Constants.organizationUri;
import static no.sikt.nva.data.report.testing.utils.generator.NviHeaders.AUTHOR_SHARE_COUNT;
import static no.sikt.nva.data.report.testing.utils.generator.NviHeaders.GLOBAL_APPROVAL_STATUS;
import static no.sikt.nva.data.report.testing.utils.generator.NviHeaders.INSTITUTION_APPROVAL_STATUS;
import static no.sikt.nva.data.report.testing.utils.generator.NviHeaders.INSTITUTION_POINTS;
import static no.sikt.nva.data.report.testing.utils.generator.NviHeaders.INTERNATIONAL_COLLABORATION_FACTOR;
import static no.sikt.nva.data.report.testing.utils.generator.NviHeaders.IS_APPLICABLE;
import static no.sikt.nva.data.report.testing.utils.generator.NviHeaders.POINTS_FOR_AFFILIATION;
import static no.sikt.nva.data.report.testing.utils.generator.NviHeaders.PUBLICATION_TYPE_CHANNEL_LEVEL_POINTS;
import static no.sikt.nva.data.report.testing.utils.generator.NviHeaders.REPORTED_PERIOD;
import static no.sikt.nva.data.report.testing.utils.generator.NviHeaders.TOTAL_POINTS;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.AFFILIATION_ID;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.INSTITUTION_ID;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.MODIFIED_DATE;
import static no.sikt.nva.data.report.testing.utils.generator.PublicationHeaders.PUBLICATION_ID;
import static no.sikt.nva.data.report.testing.utils.generator.SampleData.SOME_SUB_UNIT_IDENTIFIER;
import static no.sikt.nva.data.report.testing.utils.generator.SampleData.SOME_TOP_LEVEL_IDENTIFIER;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleApproval;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleApprovalStatus;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleCreatorAffiliationPoints;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleGlobalApprovalStatus;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleInstitutionPoints;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleNviCandidate;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleNviCandidate.Builder;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleNviContributor;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleNviOrganization;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SamplePublicationDetails;
import no.sikt.nva.data.report.testing.utils.generator.publication.SamplePublication;

@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
public final class NviSampleData {

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
                                                           IS_APPLICABLE,
                                                           MODIFIED_DATE);
    public static final String SOME_YEAR = "2023";
    private static final BigDecimal MIN_BIG_DECIMAL = BigDecimal.ZERO;
    private static final BigDecimal MAX_BIG_DECIMAL = BigDecimal.TEN;
    public static final String SOME_OTHER_TOP_LEVEL_IDENTIFIER = "90.0.0.0";

    private NviSampleData() {
        // NO-OP
    }

    public static BigDecimal randomBigDecimal() {
        var randomBigDecimal = MIN_BIG_DECIMAL.add(
            BigDecimal.valueOf(Math.random()).multiply(MAX_BIG_DECIMAL.subtract(MIN_BIG_DECIMAL)));
        return randomBigDecimal.setScale(4, RoundingMode.HALF_UP);
    }

    public static SampleNviCandidate generateNviCandidate(Instant modifiedDate, SamplePublication publication) {
        var publicationDetails = generatePublicationDetails(publication);
        var approvals = generateApprovals(publicationDetails);
        return getCandidateBuilder(true, modifiedDate, publicationDetails, approvals, SOME_YEAR).build();
    }

    static List<SampleNviCandidate> generateNviData(List<SamplePublication> publications) {
        var dataSet = new ArrayList<SampleNviCandidate>();
        for (SamplePublication publication : publications) {
            var modifiedDate = publication.getModifiedDate();
            addApplicableNviCandidate(publication, modifiedDate, dataSet);
            var reportingYear = publication.getDate().year();
            addNonApplicableNviCandidate(modifiedDate, reportingYear, dataSet);
            addReportedCandidate(modifiedDate, reportingYear, dataSet);
            addCoPublishedCandidate(modifiedDate, reportingYear, dataSet);
        }
        return dataSet;
    }

    private static void addApplicableNviCandidate(SamplePublication publication, Instant modifiedDate,
                                                  List<SampleNviCandidate> dataSet) {
        var nviCandidate = generateNviCandidate(modifiedDate, publication);
        dataSet.add(nviCandidate);
    }

    private static void addCoPublishedCandidate(Instant modifiedDate, String reportingYear,
                                                List<SampleNviCandidate> dataSet) {
        var coPublishedCandidate = generateCoPublishedNviCandidate(modifiedDate, reportingYear);
        dataSet.add(coPublishedCandidate);
    }

    private static void addReportedCandidate(Instant modifiedDate, String reportingYear,
                                             List<SampleNviCandidate> dataSet) {
        var reportedCandidate = generateReportedNviCandidate(modifiedDate, reportingYear);
        dataSet.add(reportedCandidate);
    }

    private static void addNonApplicableNviCandidate(Instant modifiedDate, String reportingYear,
                                                     List<SampleNviCandidate> dataSet) {
        var nonApplicableNviCandidate = generateNonApplicableNviCandidate(modifiedDate, reportingYear);
        dataSet.add(nonApplicableNviCandidate);
    }

    private static SampleNviCandidate generateReportedNviCandidate(Instant modifiedDate, String reportingYear) {
        var publicationDetails = generatePublicationDetails();
        var approvals = generateApprovals(publicationDetails);
        return getCandidateBuilder(true, modifiedDate, publicationDetails, approvals, reportingYear)
                   .withReported(true)
                   .build();
    }

    private static SampleNviCandidate generateCoPublishedNviCandidate(Instant modifiedDate, String reportingYear) {
        var publicationDetails = SamplePublicationDetails.builder()
                                     .withId(randomUri().toString())
                                     .withContributors(
                                         new ArrayList<>(List.of(
                                             generateNviContributor(randomUri().toString(), SOME_TOP_LEVEL_IDENTIFIER),
                                             generateNviContributor(randomUri().toString(),
                                                                    SOME_OTHER_TOP_LEVEL_IDENTIFIER))))
                                     .build();
        var approvals = generateApprovals(publicationDetails);
        return getCandidateBuilder(true, modifiedDate, publicationDetails, approvals, reportingYear).build();
    }

    @SuppressWarnings("unchecked")
    private static SampleNviCandidate generateNonApplicableNviCandidate(Instant modifiedDate, String reportingYear) {
        return getCandidateBuilder(false, modifiedDate, generatePublicationDetails(),
                                   Collections.EMPTY_LIST,
                                   reportingYear).build();
    }

    private static SamplePublicationDetails generatePublicationDetails() {
        return generatePublicationDetails(randomUri().toString());
    }

    private static SamplePublicationDetails generatePublicationDetails(SamplePublication publication) {
        var contributors = publication.getContributors().stream()
                               .map(contributor -> generateNviContributor(contributor.getIdentity().uri(),
                                                                          SOME_SUB_UNIT_IDENTIFIER))
                               .toList();
        return SamplePublicationDetails.builder()
                   .withId(publication.getPublicationUri())
                   .withContributors(new ArrayList<>(contributors))
                   .build();
    }

    private static SamplePublicationDetails generatePublicationDetails(String publicationId) {
        return SamplePublicationDetails.builder()
                   .withId(publicationId)
                   .withContributors(new ArrayList<>(List.of(
                       generateNviContributor(randomUri().toString(), SOME_SUB_UNIT_IDENTIFIER),
                       generateNviContributor(randomUri().toString(), SOME_TOP_LEVEL_IDENTIFIER))))
                   .build();
    }

    private static SampleNviContributor generateNviContributor(String id, String organizationIdentifier) {
        return SampleNviContributor.builder()
                   .withId(id)
                   .withAffiliations(List.of(generateNviAffiliation(organizationIdentifier)))
                   .build();
    }

    private static SampleNviOrganization generateNviAffiliation(String organizationIdentifier) {
        return SampleNviOrganization.builder()
                   .withId(organizationUri(organizationIdentifier))
                   .build();
    }

    private static List<SampleApproval> generateApprovals(SamplePublicationDetails publicationDetails) {
        return publicationDetails.contributors().stream()
                   .flatMap(contributor -> contributor.affiliations().stream())
                   .map(SampleNviOrganization::getTopLevelOrganization)
                   .distinct()
                   .map(topLevelOrganization -> generateApproval(topLevelOrganization, publicationDetails))
                   .toList();
    }

    private static SampleApproval generateApproval(String topLevelOrganization,
                                                   SamplePublicationDetails publicationDetails) {
        var nviContributors = publicationDetails.filterContributorsWithTopLevelOrg(topLevelOrganization);
        var involvedOrgs = getAffiliations(topLevelOrganization, nviContributors);
        involvedOrgs.add(topLevelOrganization);
        return SampleApproval.builder()
                   .withInstitutionId(URI.create(topLevelOrganization))
                   .withApprovalStatus(randomElement(SampleApprovalStatus.values()))
                   .withPoints(generateInstitutionPoints(topLevelOrganization, nviContributors))
                   .withInvolvedOrganizations(involvedOrgs)
                   .build();
    }

    private static Set<String> getAffiliations(String topLevelOrganization,
                                               List<SampleNviContributor> nviContributors) {
        return nviContributors.stream()
                   .flatMap(contributor -> contributor.filterAffiliationsWithTopLevelOrg(topLevelOrganization).stream())
                   .map(SampleNviOrganization::id)
                   .collect(Collectors.toCollection(HashSet::new));
    }

    private static SampleInstitutionPoints generateInstitutionPoints(String topLevelOrganization,
                                                                     List<SampleNviContributor> nviContributors) {
        return SampleInstitutionPoints.builder()
                   .withCreatorAffiliationPoints(generateCreatorAffiliationPointsList(topLevelOrganization,
                                                                                      nviContributors))
                   .withInstitutionPoints(randomBigDecimal())
                   .build();
    }

    private static List<SampleCreatorAffiliationPoints> generateCreatorAffiliationPointsList(
        String topLevelOrganization,
        List<SampleNviContributor> nviContributors) {
        return nviContributors.stream()
                   .map(creator -> getCreatorAffiliationPoints(topLevelOrganization, creator))
                   .flatMap(List::stream)
                   .toList();
    }

    private static List<SampleCreatorAffiliationPoints> getCreatorAffiliationPoints(String topLevelOrganization,
                                                                                    SampleNviContributor creator) {
        return creator.filterAffiliationsWithTopLevelOrg(topLevelOrganization).stream()
                   .map(affiliation -> generateCreatorAffiliationPoints(creator, affiliation))
                   .toList();
    }

    private static SampleCreatorAffiliationPoints generateCreatorAffiliationPoints(SampleNviContributor creator,
                                                                                   SampleNviOrganization affiliation) {
        return SampleCreatorAffiliationPoints.builder()
                   .withNviCreator(URI.create(creator.id()))
                   .withAffiliationId(URI.create(affiliation.id()))
                   .withPoints(randomBigDecimal())
                   .build();
    }

    private static Builder getCandidateBuilder(boolean isApplicable, Instant modifiedDate,
                                               SamplePublicationDetails publicationDetails,
                                               List<SampleApproval> approvals, String reportingPeriod) {
        var identifier = UUID.randomUUID();
        return SampleNviCandidate.builder()
                   .withIsApplicable(isApplicable)
                   .withIdentifier(identifier.toString())
                   .withCandidateUri(Constants.candidateUri(identifier))
                   .withModifiedDate(modifiedDate)
                   .withPublicationDetails(publicationDetails)
                   .withApprovals(approvals)
                   .withCreatorShareCount(countCombinationsOfCreatorsAndAffiliations(publicationDetails))
                   .withInternationalCollaborationFactor(randomBigDecimal())
                   .withGlobalApprovalStatus(randomElement(SampleGlobalApprovalStatus.values()))
                   .withPublicationTypeChannelLevelPoints(randomBigDecimal())
                   .withTotalPoints(randomBigDecimal())
                   .withReportingPeriod(reportingPeriod);
    }

    private static int countCombinationsOfCreatorsAndAffiliations(SamplePublicationDetails publicationDetails) {
        return publicationDetails.contributors()
                   .stream()
                   .flatMap(contributor -> contributor.affiliations().stream())
                   .toList()
                   .size();
    }
}
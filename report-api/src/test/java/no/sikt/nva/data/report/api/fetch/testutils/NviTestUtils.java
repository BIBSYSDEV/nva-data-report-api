package no.sikt.nva.data.report.api.fetch.testutils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestApproval;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestCreatorAffiliationPoints;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviContributor;
import no.sikt.nva.data.report.api.fetch.testutils.generator.nvi.TestNviOrganization;

public final class NviTestUtils {

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int NVI_POINT_SCALE = 4;

    public static BigDecimal getExpectedPointsForAffiliation(TestNviOrganization affiliation,
                                                             TestNviContributor contributor, TestApproval approval) {
        return approval.points()
                   .creatorAffiliationPoints()
                   .stream()
                   .filter(pointsForAffiliation -> isForCreatorAndAffiliation(affiliation, contributor,
                                                                              pointsForAffiliation))
                   .findFirst()
                   .map(TestCreatorAffiliationPoints::points)
                   .map(points -> points.stripTrailingZeros().setScale(NVI_POINT_SCALE, ROUNDING_MODE))
                   .orElseThrow();
    }

    private static boolean isForCreatorAndAffiliation(TestNviOrganization affiliation, TestNviContributor contributor,
                                                      TestCreatorAffiliationPoints creatorAffiliationPoints) {
        return creatorAffiliationPoints.creatorId().toString().equals(contributor.id()) &&
               creatorAffiliationPoints.affiliationId().toString().equals(affiliation.id());
    }
}

package no.sikt.nva.data.report.testing.utils.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleApproval;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleCreatorAffiliationPoints;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleNviContributor;
import no.sikt.nva.data.report.testing.utils.generator.nvi.SampleNviOrganization;

public final class NviTestUtils {

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int NVI_POINT_SCALE = 4;

    public static BigDecimal getExpectedPointsForAffiliation(SampleNviOrganization affiliation,
                                                             SampleNviContributor contributor, SampleApproval approval) {
        return approval.points()
                   .creatorAffiliationPoints()
                   .stream()
                   .filter(pointsForAffiliation -> isForCreatorAndAffiliation(affiliation, contributor,
                                                                              pointsForAffiliation))
                   .findFirst()
                   .map(SampleCreatorAffiliationPoints::points)
                   .map(points -> points.stripTrailingZeros().setScale(NVI_POINT_SCALE, ROUNDING_MODE))
                   .orElseThrow();
    }

    private static boolean isForCreatorAndAffiliation(SampleNviOrganization affiliation, SampleNviContributor contributor,
                                                      SampleCreatorAffiliationPoints creatorAffiliationPoints) {
        return creatorAffiliationPoints.nviCreator().toString().equals(contributor.id())
               && creatorAffiliationPoints.affiliationId().toString().equals(affiliation.id());
    }
}

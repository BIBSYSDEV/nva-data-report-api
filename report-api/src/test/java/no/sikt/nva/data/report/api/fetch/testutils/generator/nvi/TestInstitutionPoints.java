package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import java.math.BigDecimal;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.InstitutionPointsGenerator;

public record TestInstitutionPoints(BigDecimal points,
                                    List<TestCreatorAffiliationPoints> creatorAffiliationPoints) {

    public static Builder builder() {
        return new Builder();
    }

    public InstitutionPointsGenerator toModel() {
        return new InstitutionPointsGenerator();
    }

    public static final class Builder {

        private BigDecimal points;
        private List<TestCreatorAffiliationPoints> creatorAffiliationPoints;

        private Builder() {
        }

        public Builder withPoints(BigDecimal points) {
            this.points = points;
            return this;
        }

        public Builder withCreatorAffiliationPoints(List<TestCreatorAffiliationPoints> creatorAffiliationPoints) {
            this.creatorAffiliationPoints = creatorAffiliationPoints;
            return this;
        }

        public TestInstitutionPoints build() {
            return new TestInstitutionPoints(points, creatorAffiliationPoints);
        }
    }
}

package no.sikt.nva.data.report.testing.utils.generator.nvi;

import java.math.BigDecimal;
import java.util.List;
import no.sikt.nva.data.report.testing.utils.generator.model.nvi.InstitutionPointsGenerator;

public record SampleInstitutionPoints(BigDecimal institutionPoints,
                                      List<SampleCreatorAffiliationPoints> creatorAffiliationPoints) {

    public static Builder builder() {
        return new Builder();
    }

    public InstitutionPointsGenerator toModel() {
        return new InstitutionPointsGenerator();
    }

    public static final class Builder {

        private BigDecimal points;
        private List<SampleCreatorAffiliationPoints> creatorAffiliationPoints;

        private Builder() {
        }

        public Builder withInstitutionPoints(BigDecimal points) {
            this.points = points;
            return this;
        }

        public Builder withCreatorAffiliationPoints(List<SampleCreatorAffiliationPoints> creatorAffiliationPoints) {
            this.creatorAffiliationPoints = creatorAffiliationPoints;
            return this;
        }

        public SampleInstitutionPoints build() {
            return new SampleInstitutionPoints(points, creatorAffiliationPoints);
        }
    }
}

package no.sikt.nva.data.report.testing.utils.generator.nvi;

import java.math.BigDecimal;
import java.net.URI;
import no.sikt.nva.data.report.testing.utils.generator.model.nvi.CreatorAffiliationPointsGenerator;

public record SampleCreatorAffiliationPoints(URI nviCreator,
                                             URI affiliationId,
                                             BigDecimal points) {

    public static Builder builder() {
        return new Builder();
    }

    public CreatorAffiliationPointsGenerator toModel() {
        return new CreatorAffiliationPointsGenerator();
    }

    public static final class Builder {

        private URI nviCreator;
        private URI affiliationId;
        private BigDecimal points;

        private Builder() {
        }

        public Builder withNviCreator(URI nviCreator) {
            this.nviCreator = nviCreator;
            return this;
        }

        public Builder withAffiliationId(URI affiliationId) {
            this.affiliationId = affiliationId;
            return this;
        }

        public Builder withPoints(BigDecimal points) {
            this.points = points;
            return this;
        }

        public SampleCreatorAffiliationPoints build() {
            return new SampleCreatorAffiliationPoints(nviCreator, affiliationId, points);
        }
    }
}

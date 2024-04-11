package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import java.math.BigDecimal;
import java.net.URI;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.CreatorAffiliationPointsGenerator;

public record TestCreatorAffiliationPoints(URI creatorId,
                                           URI affiliationId,
                                           BigDecimal points) {

    public static Builder builder() {
        return new Builder();
    }

    public CreatorAffiliationPointsGenerator toModel() {
        return new CreatorAffiliationPointsGenerator();
    }

    public static final class Builder {

        private URI creatorId;
        private URI affiliationId;
        private BigDecimal points;

        private Builder() {
        }

        public Builder withCreatorId(URI creatorId) {
            this.creatorId = creatorId;
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

        public TestCreatorAffiliationPoints build() {
            return new TestCreatorAffiliationPoints(creatorId, affiliationId, points);
        }
    }
}

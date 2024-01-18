package no.sikt.nva.data.report.api.etl.testutils.model.nvi;

import java.util.UUID;

public record ConsumptionAttributes(UUID documentIdentifier, String index) {

    public static ConsumptionAttributes from(UUID documentIdentifier) {
        return new ConsumptionAttributes(documentIdentifier, "notRelevant");
    }
}

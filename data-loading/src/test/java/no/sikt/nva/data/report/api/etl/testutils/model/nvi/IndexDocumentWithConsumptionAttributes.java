package no.sikt.nva.data.report.api.etl.testutils.model.nvi;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.UUID;

@JsonSerialize
public record IndexDocumentWithConsumptionAttributes(
    @JsonProperty(BODY) JsonNode indexDocument,
    @JsonProperty(CONSUMPTION_ATTRIBUTES) ConsumptionAttributes consumptionAttributes) {

    private static final String CONSUMPTION_ATTRIBUTES = "consumptionAttributes";
    private static final String BODY = "body";

    public static IndexDocumentWithConsumptionAttributes from(JsonNode document, UUID identifier) {
        return new IndexDocumentWithConsumptionAttributes(document, ConsumptionAttributes.from(identifier));
    }

    public String toJsonString() {
        return attempt(() -> dtoObjectMapper.writeValueAsString(this)).orElseThrow();
    }
}

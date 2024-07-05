package no.sikt.nva.data.report.api.etl.transformer.model;

import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Optional;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.identifiers.SortableIdentifier;
import nva.commons.core.JacocoGenerated;

public class IndexDocument implements JsonSerializable {

    public static final ObjectMapper objectMapper = JsonUtils.dtoObjectMapper;
    public static final String BODY = "body";
    public static final String CONSUMPTION_ATTRIBUTES = "consumptionAttributes";
    public static final String MISSING_IDENTIFIER_IN_RESOURCE = "Missing identifier in resource";
    @JsonProperty(CONSUMPTION_ATTRIBUTES)
    private final EventConsumptionAttributes consumptionAttributes;
    @JsonProperty(BODY)
    private final JsonNode resource;

    @JsonCreator
    public IndexDocument(@JsonProperty(CONSUMPTION_ATTRIBUTES) EventConsumptionAttributes consumptionAttributes,
                         @JsonProperty(BODY) JsonNode resource) {
        this.consumptionAttributes = consumptionAttributes;
        this.resource = resource;
    }

    public static IndexDocument fromJsonString(String json) {
        return attempt(() -> objectMapper.readValue(json, IndexDocument.class)).orElseThrow();
    }

    @JacocoGenerated
    public JsonNode getResource() {
        return resource;
    }

    public URI getResourceId() {
        return URI.create(resource.at("/id").asText());
    }

    @JsonIgnore
    public String getDocumentIdentifier() {
        return Optional.ofNullable(consumptionAttributes.getDocumentIdentifier())
                   .map(SortableIdentifier::toString)
                   .orElseThrow(() -> new RuntimeException(MISSING_IDENTIFIER_IN_RESOURCE));
    }
}

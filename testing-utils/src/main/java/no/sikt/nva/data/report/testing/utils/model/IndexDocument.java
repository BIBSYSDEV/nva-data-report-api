package no.sikt.nva.data.report.testing.utils.model;

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
import no.unit.nva.s3.S3Driver;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;

public class IndexDocument implements JsonSerializable {

    public static final ObjectMapper objectMapper = JsonUtils.dtoObjectMapper;
    public static final String BODY = "body";
    public static final String CONSUMPTION_ATTRIBUTES = "consumptionAttributes";
    public static final String MISSING_IDENTIFIER_IN_RESOURCE = "Missing identifier in resource";
    public static final String NVI_INDEX = "nvi-candidates";
    public static final String PUBLICATION_INDEX = "resources";
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

    public static IndexDocument from(NviIndexDocument nviIndexDocument) {
        return new IndexDocument(new EventConsumptionAttributes(NVI_INDEX, nviIndexDocument.identifier()),
                                 nviIndexDocument.asJsonNode());
    }

    public static IndexDocument from(PublicationIndexDocument publicationIndexDocument) {
        return new IndexDocument(
            new EventConsumptionAttributes(PUBLICATION_INDEX, publicationIndexDocument.identifier()),
            publicationIndexDocument.asJsonNode());
    }

    @JacocoGenerated
    public JsonNode getResource() {
        return resource;
    }

    public URI getResourceId() {
        return URI.create(resource.at("/id").asText());
    }

    @JsonIgnore
    public String getIdentifier() {
        return Optional.ofNullable(consumptionAttributes.documentIdentifier())
                   .orElseThrow(() -> new RuntimeException(MISSING_IDENTIFIER_IN_RESOURCE));
    }

    public void persistInS3(S3Driver s3Driver) {
        attempt(() -> s3Driver.insertFile(UnixPath.of(getIdentifier()), toJsonString())).orElseThrow();
    }

    public String getIndex() {
        return consumptionAttributes.index();
    }
}

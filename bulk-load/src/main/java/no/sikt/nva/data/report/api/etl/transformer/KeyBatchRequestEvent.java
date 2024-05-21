package no.sikt.nva.data.report.api.etl.transformer;

import static java.util.Objects.nonNull;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.events.models.EventBody;
import nva.commons.core.JacocoGenerated;

public class KeyBatchRequestEvent implements JsonSerializable {

    private static final String START_MARKER_JSON_NAME = "startMarker";
    private static final String LOCATION_JSON_NAME = "location";
    private static final String DEFAULT_LOCATION = "resources";

    @JsonProperty(START_MARKER_JSON_NAME)
    private final String startMarker;
    @JsonProperty(LOCATION_JSON_NAME)
    private final String location;

    protected KeyBatchRequestEvent() {
        this(null, null);
    }

    @JsonCreator
    public KeyBatchRequestEvent(@JsonProperty(START_MARKER_JSON_NAME) String startMarker,
                                @JsonProperty(LOCATION_JSON_NAME) String location) {
        this.startMarker = startMarker;
        this.location = location;
    }

    public static KeyBatchRequestEvent fromJsonString(String body) throws JsonProcessingException {
        return dtoObjectMapper.readValue(body, KeyBatchRequestEvent.class);
    }

    public String getLocation() {
        return nonNull(location) ? location : DEFAULT_LOCATION;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KeyBatchRequestEvent that = (KeyBatchRequestEvent) o;
        return Objects.equals(getStartMarker(), that.getStartMarker())
               && Objects.equals(getLocation(), that.getLocation());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getStartMarker(), getLocation());
    }
    public String getStartMarker() {
        return startMarker;
    }
}

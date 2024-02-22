package no.sikt.nva.data.report.api.etl.transformer;

import static java.util.Objects.nonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.events.models.EventBody;
import nva.commons.core.JacocoGenerated;

public class KeyBatchRequestEvent implements JsonSerializable, EventBody {

    private static final String START_MARKER_JSON_NAME = "startMarker";
    private static final String TOPIC_JSON_NAME = "topic";
    private static final String LOCATION_JSON_NAME = "location";
    private static final String DEFAULT_LOCATION = "resources";

    @JsonProperty(START_MARKER_JSON_NAME)
    private final String startMarker;
    @JsonProperty(TOPIC_JSON_NAME)
    private final String topic;
    @JsonProperty(LOCATION_JSON_NAME)
    private final String location;

    @JsonCreator
    public KeyBatchRequestEvent(@JsonProperty(START_MARKER_JSON_NAME) String startMarker,
                                @JsonProperty(TOPIC_JSON_NAME) String topic,
                                @JsonProperty(LOCATION_JSON_NAME) String location) {
        this.startMarker = startMarker;
        this.topic = topic;
        this.location = location;
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
               && Objects.equals(getTopic(), that.getTopic())
               && Objects.equals(getLocation(), that.getLocation());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getStartMarker(), getTopic(), getLocation());
    }

    @Override
    public String getTopic() {
        return topic;
    }

    public String getStartMarker() {
        return startMarker;
    }
}

package no.sikt.nva.data.report.api.etl.transformer;

import static java.util.Objects.nonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.events.models.EventBody;
import nva.commons.core.JacocoGenerated;

public class KeyBatchRequestEvent implements JsonSerializable, EventBody {

    public static final String CONTINUATION_TOKEN = "continuationToken";
    private static final String START_MARKER_JSON_NAME = "startMarker";
    private static final String TOPIC_JSON_NAME = "topic";
    private static final String LOCATION_JSON_NAME = "location";
    private static final String DEFAULT_LOCATION = "resources";
    @JsonProperty(START_MARKER_JSON_NAME)
    private final String startMarker;
    @JsonProperty(CONTINUATION_TOKEN)
    private final String continuationToken;
    @JsonProperty(TOPIC_JSON_NAME)
    private final String topic;
    @JsonProperty(LOCATION_JSON_NAME)
    private final String location;
    protected KeyBatchRequestEvent() {
        this(null, null, null, null);
    }

    @JsonCreator
    public KeyBatchRequestEvent(@JsonProperty(START_MARKER_JSON_NAME) String startMarker,
                                @JsonProperty(CONTINUATION_TOKEN) String continuationToken,
                                @JsonProperty(TOPIC_JSON_NAME) String topic,
                                @JsonProperty(LOCATION_JSON_NAME) String location) {
        this.startMarker = startMarker;
        this.continuationToken = continuationToken;
        this.topic = topic;
        this.location = location;
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
        return Objects.equals(startMarker, that.startMarker)
               && Objects.equals(continuationToken, that.continuationToken)
               && Objects.equals(topic, that.topic)
               && Objects.equals(location, that.location);
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(startMarker, continuationToken, topic, location);
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public String getLocation() {
        return nonNull(location) ? location : DEFAULT_LOCATION;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    public String getStartMarker() {
        return startMarker;
    }
}

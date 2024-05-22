package no.sikt.nva.data.report.api.etl.transformer;

import static java.util.Objects.nonNull;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

public class KeyBatchRequestEvent implements JsonSerializable {

    public static final String CONTINUATION_TOKEN = "continuationToken";
    private static final String LOCATION_JSON_NAME = "location";
    private static final String DEFAULT_LOCATION = "resources";
    @JsonProperty(CONTINUATION_TOKEN)
    private final String continuationToken;
    @JsonProperty(LOCATION_JSON_NAME)
    private final String location;

    protected KeyBatchRequestEvent() {
        this(null, null);
    }

    @JsonCreator
    public KeyBatchRequestEvent(@JsonProperty(CONTINUATION_TOKEN) String continuationToken,
                                @JsonProperty(LOCATION_JSON_NAME) String location) {
        this.continuationToken = continuationToken;
        this.location = location;
    }

    public static KeyBatchRequestEvent fromJsonString(String body) {
        return attempt(() -> dtoObjectMapper.readValue(body, KeyBatchRequestEvent.class)).orElseThrow();
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(continuationToken, location);
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
        return Objects.equals(continuationToken, that.continuationToken)
               && Objects.equals(location, that.location);
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public String getLocation() {
        return nonNull(location) ? location : DEFAULT_LOCATION;
    }
}
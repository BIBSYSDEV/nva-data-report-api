package no.sikt.nva.data.report.api.etl.testutils.model.nvi;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.net.URI;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
@JsonTypeName("NviCandidate")
public record NviCandidateIndexDocument(@JsonProperty(CONTEXT) String context,
                                        URI id,
                                        UUID identifier) {

    private static final String CONTEXT = "@context";

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String context;
        private URI id;
        private UUID identifier;

        private Builder() {
        }

        public Builder withContext(String context) {
            this.context = context;
            return this;
        }

        public Builder withId(URI id) {
            this.id = id;
            return this;
        }

        public Builder withIdentifier(UUID identifier) {
            this.identifier = identifier;
            return this;
        }

        public NviCandidateIndexDocument build() {
            return new NviCandidateIndexDocument(context, id, identifier);
        }
    }
}
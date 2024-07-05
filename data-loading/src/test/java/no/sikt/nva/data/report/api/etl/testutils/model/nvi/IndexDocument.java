package no.sikt.nva.data.report.api.etl.testutils.model.nvi;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.net.URI;
import java.util.UUID;
import no.unit.nva.commons.json.JsonSerializable;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
public record IndexDocument(@JsonProperty(CONTEXT) String context,
                            URI id,
                            UUID identifier,
                            String someProperty) implements JsonSerializable {

    private static final String CONTEXT = "@context";

    public Builder copy() {
        return builder()
                   .withContext(context)
                   .withId(id)
                   .withIdentifier(identifier)
                   .withSomeProperty(someProperty);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String context;
        private URI id;
        private UUID identifier;
        private String someProperty;

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

        public Builder withSomeProperty(String someProperty) {
            this.someProperty = someProperty;
            return this;
        }

        public IndexDocument build() {
            return new IndexDocument(context, id, identifier, someProperty);
        }
    }
}
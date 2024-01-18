package no.sikt.nva.data.report.api.etl.testutils.model.nvi;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSerialize
public record PublicationDetails(String id, String type, String title, String publicationDate) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String id;
        private String type;
        private String title;
        private String publicationDate;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withPublicationDate(String publicationDate) {
            this.publicationDate = publicationDate;
            return this;
        }

        public PublicationDetails build() {
            return new PublicationDetails(id, type, title, publicationDate);
        }
    }
}

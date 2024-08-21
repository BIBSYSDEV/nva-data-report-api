package no.sikt.nva.data.report.testing.utils.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

public record EventConsumptionAttributes(@JsonProperty(INDEX_FIELD) String index,
                                         @JsonProperty(DOCUMENT_IDENTIFIER) String documentIdentifier) {

    public static final String INDEX_FIELD = "index";
    public static final String DOCUMENT_IDENTIFIER = "documentIdentifier";

    @Override
    public String index() {
        return index;
    }

    @Override
    public String documentIdentifier() {
        return documentIdentifier;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventConsumptionAttributes that)) {
            return false;
        }
        return Objects.equals(index, that.index) && Objects.equals(documentIdentifier, that.documentIdentifier);
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(index, documentIdentifier);
    }
}

package no.sikt.nva.data.report.testing.utils.generator.nvi;

import static java.util.Objects.isNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import no.sikt.nva.data.report.testing.utils.generator.model.nvi.NviOrganizationGenerator;
import nva.commons.core.paths.UriWrapper;

public record SampleNviOrganization(String id, List<String> partOf) {

    public static final String IDENTIFIER_DELIMITER = ".";
    public static final String TOP_LEVEL_SUFFIX = ".0.0.0";

    public static Builder builder() {
        return new Builder();
    }

    public NviOrganizationGenerator toModel() {
        return new NviOrganizationGenerator(id).withPartOf(partOf);
    }

    public String getTopLevelOrganization() {
        return partOf.isEmpty() ? id : partOf.stream().min(Comparator.naturalOrder()).get();
    }

    public String getOrganizationNumber() {
        var identifier = getIdentifier();
        return identifier.substring(0, identifier.indexOf(IDENTIFIER_DELIMITER));
    }

    public String getSubUnitOneNumber() {
        var identifier = getIdentifier();
        var subUnitOne = identifier.substring(identifier.indexOf(IDENTIFIER_DELIMITER) + 1);
        return subUnitOne.substring(0, identifier.indexOf(IDENTIFIER_DELIMITER) - 1);
    }

    public String getSubUnitTwoNumber() {
        var identifier = getIdentifier();
        var subUnitOne = identifier.substring(identifier.indexOf(IDENTIFIER_DELIMITER) + 1);
        var subUnitTwo = subUnitOne.substring(subUnitOne.indexOf(IDENTIFIER_DELIMITER) + 1);
        return subUnitTwo.substring(0, identifier.indexOf(IDENTIFIER_DELIMITER) - 1);
    }

    public String getSubUnitThreeNumber() {
        var identifier = getIdentifier();
        return identifier.substring(identifier.lastIndexOf(IDENTIFIER_DELIMITER) + 1);
    }

    private String getIdentifier() {
        return UriWrapper.fromUri(id).getLastPathElement();
    }

    public static final class Builder {

        private static final int MAX_PARTS_SIZE = 4;
        public static final String ZERO = "0";
        private String id;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public SampleNviOrganization build() {
            return new SampleNviOrganization(id, generatePartOfList(id));
        }

        private List<String> generatePartOfList(String id) {
            var partOfList = new ArrayList<String>();

            if (isNull(id) || id.endsWith(TOP_LEVEL_SUFFIX)) {
                return Collections.emptyList();
            }

            int lastIndexOfSlash = id.lastIndexOf('/') + 1;
            var identifier = id.substring(lastIndexOfSlash);
            var parts = identifier.split("\\.");
            if (parts.length != MAX_PARTS_SIZE) {
                throw new RuntimeException(
                    "The last path element in the organization URI should be formatted as 10.0.0.0");
            }
            final var baseUri = id.substring(0, lastIndexOfSlash);
            if (ZERO.equals(parts[1])) {
                return Collections.emptyList();
            } else if (ZERO.equals(parts[2])) {
                partOfList.add(baseUri + parts[0] + TOP_LEVEL_SUFFIX);
            } else if (ZERO.equals(parts[3])) {
                partOfList.add(baseUri + parts[0] + TOP_LEVEL_SUFFIX);
                partOfList.add(baseUri + parts[0] + "." + parts[1] + ".0.0");
            } else {
                partOfList.add(baseUri + parts[0] + TOP_LEVEL_SUFFIX);
                partOfList.add(baseUri + parts[0] + "." + parts[1] + ".0.0");
                partOfList.add(baseUri + parts[0] + "." + parts[1] + "." + parts[2] + ".0");
            }
            return partOfList;
        }
    }
}

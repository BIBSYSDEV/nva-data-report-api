package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import static java.util.Objects.isNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi.NviOrganizationGenerator;

public record TestNviOrganization(String id, List<String> partOf) {

    public static Builder builder() {
        return new Builder();
    }

    public NviOrganizationGenerator toModel() {
        return new NviOrganizationGenerator(id).withPartOf(partOf);
    }

    public String getTopLevelOrganization() {
        return partOf.isEmpty() ? id : partOf.stream().min(Comparator.naturalOrder()).get();
    }

    public static final class Builder {

        private String id;

        private Builder() {
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public TestNviOrganization build() {
            return new TestNviOrganization(id, generatePartOfList(id));
        }

        private List<String> generatePartOfList(String id) {
            var partOfList = new ArrayList<String>();

            if (isNull(id) || id.endsWith(".0.0.0")) {
                return Collections.emptyList();
            }

            int lastIndexOfSlash = id.lastIndexOf('/') + 1;
            var identifier = id.substring(lastIndexOfSlash);
            var baseUri = id.substring(0, lastIndexOfSlash);
            var parts = identifier.split("\\.");
            if (parts.length != 4) {
                throw new RuntimeException(
                    "The last path element in the organization URI should be formatted as 10.0.0.0");
            }
            if (parts[1].equals("0")) {
                return null;
            } else if (parts[2].equals("0")) {
                partOfList.add(baseUri + parts[0] + ".0.0.0");
            } else if (parts[3].equals("0")) {
                partOfList.add(baseUri + parts[0] + ".0.0.0");
                partOfList.add(baseUri + parts[0] + "." + parts[1] + ".0.0");
            } else {
                partOfList.add(baseUri + parts[0] + ".0.0.0");
                partOfList.add(baseUri + parts[0] + "." + parts[1] + ".0.0");
                partOfList.add(baseUri + parts[0] + "." + parts[1] + "." + parts[2] + ".0");
            }
            return partOfList;
        }
    }
}

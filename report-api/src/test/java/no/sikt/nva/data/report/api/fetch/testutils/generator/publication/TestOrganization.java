package no.sikt.nva.data.report.api.fetch.testutils.generator.publication;

import static java.util.Objects.isNull;
import java.util.Optional;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.publication.OrganizationGenerator;

public class TestOrganization {

    private final String id;
    private final String name;
    private final TestOrganization partOf;

    /**
     * Generates an organization structure for the affiliation based on the input id.
     *
     * @param id   A string that is a URI…lol.
     * @param name A name for the organization.
     */
    public TestOrganization(String id, String name) {
        this.id = id;
        this.name = name;
        this.partOf = generatePartOf(id);
    }

    public OrganizationGenerator toModel() {
        OrganizationGenerator organizationGenerator = new OrganizationGenerator(id).withLabel(name, "en");

        if (getPartOf().isPresent()) {
            OrganizationGenerator partOfOrganizationGenerator = getPartOf().get().toModel();
            organizationGenerator.withPartOf(partOfOrganizationGenerator);
        }

        return organizationGenerator;
    }

    private TestOrganization generatePartOf(String id) {

        if (isNull(id) || id.endsWith(".0.0.0")) {
            return null;
        }

        int lastIndexOfSlash = id.lastIndexOf('/') + 1;
        var identifier = id.substring(lastIndexOfSlash);
        var baseUri = id.substring(0, lastIndexOfSlash);
        var parts = identifier.split("\\.");
        if (parts.length != 4) {
            throw new RuntimeException("The last path element in the organization URI should be formatted as 10.0.0.0");
        }
        if (parts[1].equals("0")) {
            return null;
        } else if (parts[2].equals("0")) {
            return new TestOrganization(baseUri + parts[0] + ".0.0.0", null);
        } else if (parts[3].equals("0")) {
            return new TestOrganization(baseUri + parts[0] + "." + parts[1] + ".0.0", null);
        } else {
            return new TestOrganization(baseUri + parts[0] + "." + parts[1] + "." + parts[2] + ".0", null);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Optional<TestOrganization> getPartOf() {
        return Optional.ofNullable(partOf);
    }
}

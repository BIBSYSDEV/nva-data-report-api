package no.sikt.nva.data.report.testing.utils.generator.publication;

import static java.util.Objects.isNull;
import java.util.Optional;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.OrganizationGenerator;

public class SampleOrganization {

    public static final String ZERO = "0";
    public static final int MAX_PARTS_SIZE = 4;
    private final String id;
    private final String name;
    private final SampleOrganization partOf;

    /**
     * Generates an organization structure for the affiliation based on the input id.
     *
     * @param id   A string that is a URI…lol.
     * @param name A name for the organization.
     */
    public SampleOrganization(String id, String name) {
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Optional<SampleOrganization> getPartOf() {
        return Optional.ofNullable(partOf);
    }

    private SampleOrganization generatePartOf(String id) {

        if (isNull(id) || id.endsWith(".0.0.0")) {
            return null;
        }

        int lastIndexOfSlash = id.lastIndexOf('/') + 1;
        var identifier = id.substring(lastIndexOfSlash);
        var parts = identifier.split("\\.");
        if (parts.length != MAX_PARTS_SIZE) {
            throw new RuntimeException("The last path element in the organization URI should be formatted as 10.0.0.0");
        }
        var baseUri = id.substring(0, lastIndexOfSlash);
        if (ZERO.equals(parts[1])) {
            return null;
        } else if (ZERO.equals(parts[2])) {
            return new SampleOrganization(baseUri + parts[0] + ".0.0.0", null);
        } else if (ZERO.equals(parts[3])) {
            return new SampleOrganization(baseUri + parts[0] + "." + parts[1] + ".0.0", null);
        } else {
            return new SampleOrganization(baseUri + parts[0] + "." + parts[1] + "." + parts[2] + ".0", null);
        }
    }
}

package no.sikt.nva.data.report.api.export;

import static java.util.Objects.nonNull;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import no.sikt.nva.data.report.testing.utils.generator.publication.PublicationDate;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestContributor;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestOrganization;
import no.sikt.nva.data.report.testing.utils.generator.publication.TestPublication;

public class IndexDocumentGenerator {

    public static final String NB_FIELD = "nb";

    public static JsonNode createExpandedResource(TestPublication publication) {
        var root = objectMapper.createObjectNode();

        root.put("@context", "https://api.dev.nva.aws.unit.no/publication/context");

        root.put("id", publication.getPublicationUri());

        var entityDescription = objectMapper.createObjectNode();

        var contributors = populateAndCreateContributors(publication.getContributors());

        entityDescription.set("contributors", contributors);

        entityDescription.put("mainTitle", publication.getMainTitle());

        var publicationDate = createAndPopulatePublicationDate(publication.getDate());

        entityDescription.set("publicationDate", publicationDate);

        var reference = objectMapper.createObjectNode();

        var publicationInstance = objectMapper.createObjectNode();
        publicationInstance.put("type", publication.getPublicationCategory());

        reference.set("publicationInstance", publicationInstance);

        entityDescription.set("reference", reference);

        root.set("entityDescription", entityDescription);

        root.put("identifier", publication.getIdentifier());

        return root;
    }

    private static ArrayNode populateAndCreateContributors(List<TestContributor> contributors) {
        var contributorsNode = objectMapper.createArrayNode();
        contributors.stream().map(IndexDocumentGenerator::createContributorNode).forEach(contributorsNode::add);
        return contributorsNode;
    }

    private static ObjectNode createAndPopulatePublicationDate(PublicationDate date) {
        var publicationDate = objectMapper.createObjectNode();
        publicationDate.put("type", "PublicationDate");
        if (nonNull(date.day())) {
            publicationDate.put("day", date.day());
        }
        if (nonNull(date.month())) {
            publicationDate.put("month", date.month());
        }
        publicationDate.put("year", date.year());
        return publicationDate;
    }

    private static ObjectNode createContributorNode(TestContributor contributor) {
        var contributorNode = objectMapper.createObjectNode();

        contributorNode.put("type", "Contributor");

        var affiliations = createAndPopulateAffiliationsNode(contributor.getAffiliations());

        contributorNode.set("affiliations", affiliations);

        var role = objectMapper.createObjectNode();
        role.put("type", contributor.role());
        contributorNode.set("role", role);

        var identity = objectMapper.createObjectNode();
        identity.put("id", contributor.getIdentity().uri());
        identity.put("name", contributor.getIdentity().name());
        identity.put("orcid", randomString());

        contributorNode.set("identity", identity);
        return contributorNode;
    }

    private static ArrayNode createAndPopulateAffiliationsNode(List<TestOrganization> organizations) {
        var affiliations = objectMapper.createArrayNode();

        organizations.forEach(affiliation -> {
            var affiliationNode = objectMapper.createObjectNode();
            affiliationNode.put("id", affiliation.getId());
            affiliationNode.put("type", "Organization");
            var labels = objectMapper.createObjectNode();

            labels.put(NB_FIELD, affiliation.getName());
            affiliationNode.set("labels", labels);

            affiliations.add(affiliationNode);
        });
        return affiliations;
    }
}

package no.sikt.nva.data.report.api.fetch.testutils.generator;

import static no.sikt.nva.data.report.api.fetch.testutils.generator.Constants.PERSON_BASE_URI;
import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.EntityDescriptionGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.PublicationDateGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.PublicationGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.PublicationInstanceGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.ReferenceGenerator;
import org.apache.jena.rdf.model.Model;

public class TestPublication {

    public static final String DELIMITER = ",";
    public static final String EMPTY_STRING = "";

    private PublicationDate date;
    private Instant modifiedDate;
    private String identifier;
    private String mainTitle;
    private String publicationCategory;
    private TestChannel channel;
    private List<TestContributor> contributors;
    private List<TestFunding> fundings;
    private String publicationUri;

    public TestPublication() {
    }

    public TestPublication withModifiedDate(Instant modifiedDate) {
        this.modifiedDate = modifiedDate;
        return this;
    }

    public TestPublication withPublicationUri(String publicationUri) {
        this.publicationUri = publicationUri;
        return this;
    }

    public TestPublication withPublicationIdentifier(String publicationIdentifier) {
        this.identifier = publicationIdentifier;
        return this;
    }

    public TestPublication withPublicationTitle(String publicationTitle) {
        this.mainTitle = publicationTitle;
        return this;
    }

    public TestPublication withPublicationCategory(String publicationCategory) {
        this.publicationCategory = publicationCategory;
        return this;
    }

    public TestPublication withPublicationDate(PublicationDate publicationDate) {
        this.date = publicationDate;
        return this;
    }

    public TestPublication withContributors(List<TestContributor> contributors) {
        this.contributors = contributors;
        return this;
    }

    public TestPublication withFundings(List<TestFunding> fundings) {
        this.fundings = fundings;
        return this;
    }

    public TestPublication withChannel(TestChannel channel) {
        this.channel = channel;
        return this;
    }

    public String getExpectedAffiliationResponse() {
        var stringBuilder = new StringBuilder();
        for (TestContributor contributor : contributors) {
            for (TestOrganization affiliation : contributor.getAffiliations()) {

                var institution = extractStructuredAffiliationList(affiliation);

                stringBuilder.append(publicationUri).append(DELIMITER)
                    .append(identifier).append(DELIMITER)
                    .append(getLocalName(contributor)).append(DELIMITER)
                    .append(contributor.getIdentity().name()).append(DELIMITER)
                    .append(affiliation.getId()).append(DELIMITER)
                    .append(affiliation.getName()).append(DELIMITER)
                    .append(institution.institution).append(DELIMITER)
                    .append(institution.faculty).append(DELIMITER)
                    .append(institution.department).append(DELIMITER)
                    .append(institution.group).append(CRLF.getString());
            }
        }
        return stringBuilder.toString();
    }

    public String getExpectedContributorResponse() {
        var stringBuilder = new StringBuilder();
        for (TestContributor contributor : contributors) {
            stringBuilder.append(publicationUri).append(DELIMITER)
                .append(identifier).append(DELIMITER)
                .append(getLocalName(contributor)).append(DELIMITER)
                .append(contributor.getIdentity().name()).append(DELIMITER)
                .append(contributor.getSequenceNumber()).append(DELIMITER)
                .append(contributor.role())
                .append(CRLF.getString());
        }
        return stringBuilder.toString();
    }

    public String getExpectedFundingResponse() {
        var stringBuilder = new StringBuilder();
        for (TestFunding funding : fundings) {
            stringBuilder.append(publicationUri).append(DELIMITER)
                .append(identifier).append(DELIMITER)
                .append(funding.getFundingSource()).append(DELIMITER)
                .append(funding.getId()).append(DELIMITER)
                .append(funding.getName())
                .append(CRLF.getString());
        }
        return stringBuilder.toString();
    }

    private static String getLocalName(TestContributor contributor) {
        return contributor.getIdentity().uri().replace(PERSON_BASE_URI, EMPTY_STRING);
    }

    public Model generateModel() {
        var publicationDate = new PublicationDateGenerator()
                                  .withYear(date.year())
                                  .withMonth(date.month())
                                  .withDay(date.day());
        var reference = new ReferenceGenerator()
                            .withPublicationContext(channel.toModel())
                            .withPublicationInstance(new PublicationInstanceGenerator(publicationCategory));
        var entityDescription = new EntityDescriptionGenerator()
                                    .withPublicationDate(publicationDate)
                                    .withMainTitle(mainTitle)
                                    .withReference(reference);
        contributors.stream()
            .map(TestContributor::toModel)
            .forEach(entityDescription::withContributor);
        var publication = new PublicationGenerator(identifier, modifiedDate.toString())
                              .withEntityDescription(entityDescription);
        fundings.stream().map(TestFunding::toModel).forEach(publication::withFunding);

        return publication.build();
    }

    public String getPublicationUri() {
        return publicationUri;
    }

    private University extractStructuredAffiliationList(TestOrganization affiliation) {
        var affiliations = new ArrayList<String>();
        affiliations.add(affiliation.getId());
        var partOf = affiliation.getPartOf();
        while (partOf.isPresent()) {
            partOf = getPartOf(affiliations, partOf.get());
        }
        var institution = getItemAt(affiliations, 1);
        var faculty = getItemAt(affiliations, 2);
        var department = getItemAt(affiliations, 3);
        var group = getItemAt(affiliations, 4);
        return new University(institution, faculty, department, group);
    }

    private static String getItemAt(ArrayList<String> affiliations, int offset) {
        int size = affiliations.size();
        return (size > offset) ? affiliations.get(size - offset) : EMPTY_STRING;
    }

    private static Optional<TestOrganization> getPartOf(ArrayList<String> affiliations, TestOrganization partOf) {
        affiliations.add(partOf.getId());
        return partOf.getPartOf();
    }

    private record University(String institution, String faculty, String department, String group) {

    }
}

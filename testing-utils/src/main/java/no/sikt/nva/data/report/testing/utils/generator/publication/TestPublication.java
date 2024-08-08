package no.sikt.nva.data.report.testing.utils.generator.publication;

import static no.sikt.nva.data.report.testing.utils.generator.Constants.PERSON_BASE_URI;
import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.EntityDescriptionGenerator;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.PublicationDateGenerator;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.PublicationGenerator;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.PublicationInstanceGenerator;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.ReferenceGenerator;
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
    private String publicationStatus;

    public TestPublication() {
    }

    public PublicationDate getDate() {
        return date;
    }

    public Instant getModifiedDate() {
        return modifiedDate;
    }

    public List<TestContributor> getContributors() {
        return contributors;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getMainTitle() {
        return mainTitle;
    }

    public String getPublicationCategory() {
        return publicationCategory;
    }

    public TestChannel getChannel() {
        return channel;
    }

    public String getPublicationUri() {
        return publicationUri;
    }

    public Set<TestOrganization> getContributorAffiliations() {
        return getContributors().stream()
                   .flatMap(contributor -> contributor.getAffiliations().stream())
                   .collect(Collectors.toSet());
    }

    public TestPublication withModifiedDate(Instant modifiedDate) {
        this.modifiedDate = modifiedDate;
        return this;
    }

    public TestPublication withPublicationUri(String publicationUri) {
        this.publicationUri = publicationUri;
        return this;
    }

    public TestPublication withPublicationStatus(String status) {
        this.publicationStatus = status;
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

    public String getPublicationStatus() {
        return publicationStatus;
    }

    public String getExpectedAffiliationResponse() {
        var stringBuilder = new StringBuilder();
        for (TestContributor contributor : contributors) {
            for (TestOrganization affiliation : contributor.getAffiliations()) {

                var institution = extractStructuredAffiliationList(affiliation);

                stringBuilder.append(publicationUri).append(DELIMITER)
                    .append(publicationStatus).append(DELIMITER)
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
                .append(publicationStatus).append(DELIMITER)
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
                .append(publicationStatus).append(DELIMITER)
                .append(identifier).append(DELIMITER)
                .append(funding.getFundingSource()).append(DELIMITER)
                .append(funding.getId()).append(DELIMITER)
                .append(funding.getName())
                .append(CRLF.getString());
        }
        return stringBuilder.toString();
    }

    public String getExpectedIdentifierResponse() {
        var stringBuilder = new StringBuilder();
        for (TestFunding funding : fundings) {
            stringBuilder.append(publicationUri).append(DELIMITER)
                .append(publicationStatus).append(DELIMITER)
                .append(identifier).append(DELIMITER)
                .append(funding.getFundingSource()).append(DELIMITER)
                .append(funding.getId())
                .append(CRLF.getString());
        }
        return stringBuilder.toString();
    }

    public String getExpectedPublicationResponse() {
        var stringBuilder = new StringBuilder();
        stringBuilder.append(publicationUri).append(DELIMITER)
            .append(publicationStatus).append(DELIMITER)
            .append(mainTitle).append(DELIMITER)
            .append(publicationCategory).append(DELIMITER)
            .append(date.getIsoDate()).append(DELIMITER)
            .append(channel.getType()).append(DELIMITER)
            .append(channel.getIdentifier()).append(DELIMITER)
            .append(channel.getName()).append(DELIMITER)
            .append(channel.getOnlineIssn()).append(DELIMITER)
            .append(channel.getPrintIssn()).append(DELIMITER)
            .append(channel.getScientificValue()).append(DELIMITER)
            .append(identifier)
            .append(CRLF.getString());

        return stringBuilder.toString();
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
                              .withPublicationStatus(publicationStatus)
                              .withEntityDescription(entityDescription);
        fundings.stream().map(TestFunding::toModel).forEach(publication::withFunding);

        return publication.build();
    }

    private static String getLocalName(TestContributor contributor) {
        return contributor.getIdentity().uri().replace(PERSON_BASE_URI, EMPTY_STRING);
    }

    private static String getItemAt(ArrayList<String> affiliations, int offset) {
        int size = affiliations.size();
        return (size >= offset) ? affiliations.get(size - offset) : EMPTY_STRING;
    }

    private static Optional<TestOrganization> getPartOf(ArrayList<String> affiliations, TestOrganization partOf) {
        affiliations.add(partOf.getId());
        return partOf.getPartOf();
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

    private record University(String institution, String faculty, String department, String group) {

    }
}

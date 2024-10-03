package no.sikt.nva.data.report.testing.utils.generator.publication;

import static java.util.Objects.nonNull;
import static no.sikt.nva.data.report.testing.utils.generator.Constants.PERSON_BASE_URI;
import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SamplePublication {

    public static final String DELIMITER = ",";
    public static final String EMPTY_STRING = "";
    private PublicationDate date;
    private Instant modifiedDate;
    private String identifier;
    private String mainTitle;
    private String publicationCategory;
    private SampleChannel channel;
    private List<SampleContributor> contributors;
    private List<SampleFunding> fundings;
    private List<SampleAdditionalIdentifier> additionalIdentifiers;
    private String publicationUri;
    private String publicationStatus;

    public SamplePublication() {
    }

    public List<SampleFunding> getFundings() {
        return fundings;
    }

    public PublicationDate getDate() {
        return date;
    }

    public Instant getModifiedDate() {
        return modifiedDate;
    }

    public List<SampleContributor> getContributors() {
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

    public SampleChannel getChannel() {
        return channel;
    }

    public String getPublicationUri() {
        return publicationUri;
    }

    public Set<SampleOrganization> getContributorAffiliations() {
        return getContributors().stream()
                   .flatMap(contributor -> contributor.getAffiliations().stream())
                   .collect(Collectors.toSet());
    }

    public List<SampleAdditionalIdentifier> getAdditionalIdentifiers() {
        return additionalIdentifiers;
    }

    public SamplePublication withModifiedDate(Instant modifiedDate) {
        this.modifiedDate = modifiedDate;
        return this;
    }

    public SamplePublication withPublicationUri(String publicationUri) {
        this.publicationUri = publicationUri;
        return this;
    }

    public SamplePublication withPublicationStatus(String status) {
        this.publicationStatus = status;
        return this;
    }

    public SamplePublication withPublicationIdentifier(String publicationIdentifier) {
        this.identifier = publicationIdentifier;
        return this;
    }

    public SamplePublication withPublicationTitle(String publicationTitle) {
        this.mainTitle = publicationTitle;
        return this;
    }

    public SamplePublication withPublicationCategory(String publicationCategory) {
        this.publicationCategory = publicationCategory;
        return this;
    }

    public SamplePublication withPublicationDate(PublicationDate publicationDate) {
        this.date = publicationDate;
        return this;
    }

    public SamplePublication withContributors(List<SampleContributor> contributors) {
        this.contributors = contributors;
        return this;
    }

    public SamplePublication withFundings(List<SampleFunding> fundings) {
        this.fundings = fundings;
        return this;
    }

    public SamplePublication withChannel(SampleChannel channel) {
        this.channel = channel;
        return this;
    }

    public SamplePublication withAdditionalIdentifiers(List<SampleAdditionalIdentifier> additionalIdentifiers) {
        this.additionalIdentifiers = additionalIdentifiers;
        return this;
    }

    public String getPublicationStatus() {
        return publicationStatus;
    }

    public String getExpectedAffiliationResponse() {
        var stringBuilder = new StringBuilder();
        for (SampleContributor contributor : contributors) {
            for (SampleOrganization affiliation : contributor.getAffiliations()) {

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
                    .append(institution.group).append(DELIMITER)
                    .append(modifiedDate).append(CRLF.getString());
            }
        }
        return stringBuilder.toString();
    }

    public String getExpectedContributorResponse() {
        var stringBuilder = new StringBuilder();
        for (SampleContributor contributor : contributors) {
            stringBuilder.append(publicationUri).append(DELIMITER)
                .append(publicationStatus).append(DELIMITER)
                .append(identifier).append(DELIMITER)
                .append(getLocalName(contributor)).append(DELIMITER)
                .append(contributor.getIdentity().name()).append(DELIMITER)
                .append(contributor.getSequenceNumber()).append(DELIMITER)
                .append(contributor.role()).append(DELIMITER)
                .append(modifiedDate).append(CRLF.getString());
        }
        return stringBuilder.toString();
    }

    public String getExpectedFundingResponse() {
        var stringBuilder = new StringBuilder();
        for (SampleFunding funding : fundings) {
            stringBuilder.append(publicationUri).append(DELIMITER)
                .append(publicationStatus).append(DELIMITER)
                .append(identifier).append(DELIMITER)
                .append(funding.getFundingSource()).append(DELIMITER)
                .append(nonNull(funding.getId()) ? funding.getId() : EMPTY_STRING).append(DELIMITER)
                .append(funding.getName()).append(DELIMITER)
                .append(modifiedDate).append(CRLF.getString());
        }
        return stringBuilder.toString();
    }

    public String getExpectedIdentifierResponse() {
        var stringBuilder = new StringBuilder();
        additionalIdentifiers.stream()
            .filter(isNotHandleIdentifier())
            .forEach(additionalIdentifier ->
                         stringBuilder.append(publicationUri).append(DELIMITER)
                             .append(publicationStatus).append(DELIMITER)
                             .append(identifier).append(DELIMITER)
                             .append(additionalIdentifier.getSourceName()).append(DELIMITER)
                             .append(additionalIdentifier.getValue()).append(DELIMITER)
                             .append(additionalIdentifier.getType()).append(DELIMITER)
                             .append(modifiedDate).append(CRLF.getString()));
        return stringBuilder.toString();
    }

    private static Predicate<SampleAdditionalIdentifier> isNotHandleIdentifier() {
        return additionalIdentifier -> !additionalIdentifier.getType().equals("HandleIdentifier");
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
            .append(identifier).append(DELIMITER)
            .append(modifiedDate).append(CRLF.getString());

        return stringBuilder.toString();
    }

    private static String getLocalName(SampleContributor contributor) {
        return contributor.getIdentity().uri().replace(PERSON_BASE_URI, EMPTY_STRING);
    }

    private static String getItemAt(List<String> affiliations, int offset) {
        int size = affiliations.size();
        return (size >= offset) ? affiliations.get(size - offset) : EMPTY_STRING;
    }

    private static Optional<SampleOrganization> getPartOf(List<String> affiliations, SampleOrganization partOf) {
        affiliations.add(partOf.getId());
        return partOf.getPartOf();
    }

    private University extractStructuredAffiliationList(SampleOrganization affiliation) {
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

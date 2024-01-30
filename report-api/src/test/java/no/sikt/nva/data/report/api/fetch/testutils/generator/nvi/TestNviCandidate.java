package no.sikt.nva.data.report.api.fetch.testutils.generator.nvi;

import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.time.Instant;
import java.util.List;
import nva.commons.core.paths.UriWrapper;

public record TestNviCandidate(List<TestApproval> approvals,
                               Instant modifiedDate,
                               TestPublicationDetails publicationDetails,
                               String identifier) {

    public static final String DELIMITER = ",";
    public static final String EMPTY_STRING = "";

    public static Builder builder() {
        return new Builder();
    }

    public String getExpectedNviResponse() {
        var stringBuilder = new StringBuilder();
        publicationDetails().contributors().stream()
            .filter(TestNviContributor::isNviContributor)
            .forEach(contributor -> generateExpectedNviResponse(stringBuilder, contributor));
        return stringBuilder.toString();
    }

    private void generateExpectedNviResponse(StringBuilder stringBuilder, TestNviContributor contributor) {
        contributor.affiliations().stream()
            .filter(TestAffiliation::isNviAffiliation)
            .forEach(affiliation -> generateExpectedNviResponse(stringBuilder, contributor, affiliation));
    }

    private void generateExpectedNviResponse(StringBuilder stringBuilder, TestNviContributor contributor,
                                             TestAffiliation affiliation) {
        stringBuilder.append(publicationDetails().id()).append(DELIMITER)
            .append(extractPublicationIdentifier()).append(DELIMITER)
            .append(CRLF.getString());
    }

    private String extractPublicationIdentifier() {
        return UriWrapper.fromUri(publicationDetails().id()).getLastPathElement();
    }

    public static final class Builder {

        private List<TestApproval> approvals;

        private Builder() {
        }

        public Builder withApprovals(List<TestApproval> approvals) {
            this.approvals = approvals;
            return this;
        }

        public TestNviCandidate build() {
            return new TestNviCandidate(approvals);
        }
    }
}

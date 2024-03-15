package no.sikt.nva.data.report.api.fetch.client;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import no.unit.nva.auth.AuthorizedBackendClient;
import nva.commons.core.paths.UriWrapper;

public class NviInstitutionReportClient {

    private static final String ACCEPT_HEADER = "Accept";
    private static final String CUSTOM_DOMAIN_PATH = "report";
    private static final String INSTITUTION_PATH = "institution";
    private static final String NVI_APPROVAL_PATH = "nvi-approval";
    private static final String QUERY_PARAM_REPORTING_YEAR = "reportingYear";
    private static final String QUERY_PARAM_INSTITUTION_ID = "institutionId";
    private final AuthorizedBackendClient authorizedBackendClient;
    private final String apiHost;

    public NviInstitutionReportClient(AuthorizedBackendClient authorizedBackendClient, String apiHost) {
        this.authorizedBackendClient = authorizedBackendClient;
        this.apiHost = apiHost;
    }

    public String fetchReport(String reportingYear, String topLevelOrganization, String acceptHeader)
        throws IOException, InterruptedException {
        var request = generateRequest(reportingYear, topLevelOrganization, acceptHeader);
        var response = authorizedBackendClient.send(request, BodyHandlers.ofString(UTF_8));
        return response.body();
    }

    private Builder generateRequest(String reportingYear, String topLevelOrganization, String acceptHeader) {
        return HttpRequest.newBuilder()
                   .uri(generateUri(reportingYear, topLevelOrganization))
                   .header(ACCEPT_HEADER, acceptHeader)
                   .GET();
    }

    private URI generateUri(String reportingYear, String institutionId) {
        return UriWrapper.fromHost(apiHost)
                   .addChild(CUSTOM_DOMAIN_PATH)
                   .addChild(INSTITUTION_PATH)
                   .addChild(NVI_APPROVAL_PATH)
                   .addQueryParameter(QUERY_PARAM_REPORTING_YEAR, reportingYear)
                   .addQueryParameter(QUERY_PARAM_INSTITUTION_ID, URLEncoder.encode(institutionId, UTF_8))
                   .getUri();
    }
}

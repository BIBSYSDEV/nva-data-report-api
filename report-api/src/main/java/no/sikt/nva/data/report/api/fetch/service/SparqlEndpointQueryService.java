package no.sikt.nva.data.report.api.fetch.service;

import commons.formatter.ResponseFormatter;
import java.net.http.HttpClient;
import no.unit.nva.auth.AuthorizedBackendClient;
import no.unit.nva.auth.CognitoCredentials;
import nva.commons.core.JacocoGenerated;

//TODO: Use in FetchNviInstitutionReport handler
//TODO: Implement
@JacocoGenerated
public class SparqlEndpointQueryService implements QueryService {

    private final HttpClient httpClient;
    private final CognitoCredentials cognitoCredentials;

    public SparqlEndpointQueryService(HttpClient httpClient, CognitoCredentials cognitoCredentials) {
        this.httpClient = httpClient;
        this.cognitoCredentials = cognitoCredentials;
    }

    @Override
    public String getResult(String sparqlQuery, ResponseFormatter formatter) {
        var authorizedBackendClient = AuthorizedBackendClient.prepareWithCognitoCredentials(httpClient,
                                                                                            cognitoCredentials);
        return null;
    }
}

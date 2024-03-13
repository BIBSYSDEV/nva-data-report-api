package no.sikt.nva.data.report.api.fetch.service;

import commons.formatter.ResponseFormatter;
import java.util.Map;

public interface QueryService {
    String getResult(String sparqlTemplate, Map<String, String> replacementStrings,
                     ResponseFormatter formatter);
}

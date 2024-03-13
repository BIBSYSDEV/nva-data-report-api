package no.sikt.nva.data.report.api.fetch.service;

import java.nio.file.Path;
import java.util.Map;
import nva.commons.core.ioutils.IoUtils;

public final class SparqlQueryGenerator {

    private static final String TEMPLATE_DIRECTORY = "template";
    private static final String SPARQL = ".sparql";

    private SparqlQueryGenerator() {
        //NO-OP
    }

    public static String getSparqlQuery(String sparqlTemplate, Map<String, String> replacementStrings) {
        var template = constructPath(sparqlTemplate);
        return replaceStrings(IoUtils.stringFromResources(template), replacementStrings);
    }

    private static Path constructPath(String sparqlTemplate) {
        return Path.of(TEMPLATE_DIRECTORY, sparqlTemplate + SPARQL);
    }

    private static String replaceStrings(String sparqlString, Map<String, String> replacementStrings) {
        var resultString = sparqlString;
        for (var entry : replacementStrings.entrySet()) {
            resultString = resultString.replace(entry.getKey(), entry.getValue());
        }
        return resultString;
    }
}

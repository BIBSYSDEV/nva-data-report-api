package no.sikt.nva.data.report.api.fetch.service;

import commons.formatter.ResponseFormatter;

public interface QueryService {

    String getResult(String sparqlQuery, ResponseFormatter formatter);
}

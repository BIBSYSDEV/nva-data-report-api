package no.sikt.nva.data.report.dbtools.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record TokenResponse(Payload payload) {

    public record Payload(String token) {

    }
}

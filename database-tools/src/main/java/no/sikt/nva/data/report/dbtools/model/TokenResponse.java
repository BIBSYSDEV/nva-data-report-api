package no.sikt.nva.data.report.dbtools.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record TokenResponse (String token){

}

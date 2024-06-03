package no.sikt.nva.data.report.api.fetch;

import no.unit.nva.commons.json.JsonSerializable;

public record GenerateNviInstitutionReportRequest(String reportingYear, String topLevelOrganization,
                                                  String mediaType) implements JsonSerializable {

}

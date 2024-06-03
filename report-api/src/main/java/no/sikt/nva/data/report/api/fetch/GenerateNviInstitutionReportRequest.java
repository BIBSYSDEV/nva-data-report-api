package no.sikt.nva.data.report.api.fetch;

import java.net.URI;
import no.unit.nva.commons.json.JsonSerializable;

public record GenerateNviInstitutionReportRequest(String reportingYear,
                                                  URI topLevelOrganization,
                                                  String mediaType,
                                                  URI location) implements JsonSerializable {

}

package no.sikt.nva.data.report.api.etl.loader;

import static java.util.Objects.nonNull;
import java.util.UUID;

public record ErrorLogRequest(UUID loadId, Integer page, Integer errorsPerPage) {

    @Override
    public Integer page() {
        return nonNull(page) ? page : 1;
    }

    @Override
    public Integer errorsPerPage() {
        return nonNull(errorsPerPage) ? errorsPerPage : 3;
    }
}

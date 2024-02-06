package no.sikt.nva.data.report.api.etl.loader;

import static java.util.Objects.nonNull;
import java.net.URI;
import java.util.UUID;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;

public record ErrorLogRequest(UUID loadId, Integer page, Integer errorsPerPage) {

    @Override
    public Integer page() {
        return nonNull(page) ? page : 1;
    }

    @Override
    public Integer errorsPerPage() {
        return nonNull(errorsPerPage) ? errorsPerPage : 3;
    }

    public URI uri(String endpoint, int port) {
        return UriWrapper.fromHost(endpoint, port)
                   .addChild("loader")
                   .addChild(loadId().toString())
                   .addQueryParameter("details", "true")
                   .addQueryParameter("errors", "true")
                   .addQueryParameter("page", String.valueOf(page()))
                   .addQueryParameter("errorsPerPage", String.valueOf(errorsPerPage()))
                   .getUri();
    }
}

package no.sikt.nva.data.report.api.fetch.testutils.generator.publication;

import no.sikt.nva.data.report.api.fetch.testutils.generator.model.IdentityGenerator;

public record TestIdentity(String uri, String name) {

    public IdentityGenerator toModel() {
        return new IdentityGenerator(uri, name);
    }
}

package no.sikt.nva.data.report.testing.utils.generator.publication;

import no.sikt.nva.data.report.testing.utils.generator.model.publication.IdentityGenerator;

public record TestIdentity(String uri, String name) {

    public IdentityGenerator toModel() {
        return new IdentityGenerator(uri, name);
    }
}

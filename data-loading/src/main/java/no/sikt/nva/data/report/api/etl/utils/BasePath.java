package no.sikt.nva.data.report.api.etl.utils;

import nva.commons.core.Environment;

public final class BasePath {

    public static final String API_HOST = "API_HOST";

    private BasePath() {
        // NO-OP
    }

    public static String basePath() {
        return new Environment().readEnv(API_HOST);
    }
}

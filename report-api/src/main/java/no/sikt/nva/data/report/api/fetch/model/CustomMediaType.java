package no.sikt.nva.data.report.api.fetch.model;

import com.google.common.net.MediaType;

public final class CustomMediaType {

    private static final String TEXT = "text";
    public static final MediaType TEXT_PLAIN = MediaType.create(TEXT, "plain");
    public static final MediaType TEXT_CSV = MediaType.create(TEXT, "csv");

    private CustomMediaType() {
        // NO-OP
    }
}

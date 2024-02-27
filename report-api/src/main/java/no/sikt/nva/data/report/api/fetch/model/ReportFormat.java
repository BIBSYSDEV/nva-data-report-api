package no.sikt.nva.data.report.api.fetch.model;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import com.google.common.net.MediaType;
import java.util.Arrays;

public enum ReportFormat {
    CSV(TEXT_CSV),
    TEXT(TEXT_PLAIN),
    EXCEL(MICROSOFT_EXCEL);
    private final MediaType mediaType;

    ReportFormat(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public static ReportFormat fromMediaType(String mediaType) {
        return Arrays.stream(ReportFormat.values())
                   .filter(format -> format.getMediaType().toString().equalsIgnoreCase(mediaType))
                   .findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Unknown media type: " + mediaType));
    }

    public MediaType getMediaType() {
        return mediaType;
    }
}

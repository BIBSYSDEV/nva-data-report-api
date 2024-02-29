package no.sikt.nva.data.report.api.fetch.model;

import static com.google.common.net.MediaType.MICROSOFT_EXCEL;
import static com.google.common.net.MediaType.OOXML_SHEET;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import com.google.common.net.MediaType;
import java.util.Arrays;
import java.util.List;

public enum ReportFormat {
    CSV(List.of(TEXT_CSV)),
    TEXT(List.of(TEXT_PLAIN)),
    EXCEL(List.of(MICROSOFT_EXCEL, OOXML_SHEET));
    private final List<MediaType> supportedMediaTypes;

    ReportFormat(List<MediaType> supportedMediaTypes) {
        this.supportedMediaTypes = supportedMediaTypes;
    }

    public static ReportFormat fromMediaType(String mediaType) {
        return Arrays.stream(ReportFormat.values())
                   .filter(format -> isASupportedMediaType(mediaType, format))
                   .findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Unknown media type: " + mediaType));
    }

    private static boolean isASupportedMediaType(String mediaType, ReportFormat format) {
        return format.supportedMediaTypes.stream()
                   .anyMatch(supportedMediaType -> isEquals(mediaType, supportedMediaType));
    }

    private static boolean isEquals(String mediaType, MediaType supportedMediaType) {
        return supportedMediaType.toString().equals(mediaType);
    }
}

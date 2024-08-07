package no.sikt.nva.data.report.api.fetch.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import commons.model.ReportFormat;
import org.junit.jupiter.api.Test;

class ReportFormatTest {

    @Test
    void shouldReturnIllegalArgumentExceptionWhenMediaTypeIsUnknown() {
        var unknownMediaType = "unknownMediaType";
        var exception = assertThrows(IllegalArgumentException.class,
                                     () -> ReportFormat.fromMediaType(unknownMediaType));
        assertEquals("Unknown media type: " + unknownMediaType, exception.getMessage());
    }
}
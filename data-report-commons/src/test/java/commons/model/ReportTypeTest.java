package commons.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ReportTypeTest {

    @Test
    void shouldParseValidCandidate() {
        var candidate = "publication";
        var reportType = ReportType.parse(candidate);
        assertEquals(ReportType.PUBLICATION, reportType);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidCandidate() {
        var candidate = "invalid";
        assertThrows(IllegalArgumentException.class, () -> ReportType.parse(candidate));
    }
}
package commons.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ReportTypeTest {

    @Test
    void shouldParseValidCandidate() {
        var candidate = "publication";
        var reportType = ReportType.parse(candidate);
        assertEquals(ReportType.PUBLICATION, reportType);
    }

    @Test
    void shouldNotReturnReportTypeNvi() {
        var reportTypes = ReportType.getAllTypesExcludingNviReport();
        assertTrue(reportTypes.stream().noneMatch(ReportType.NVI::equals));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForInvalidCandidate() {
        var candidate = "invalid";
        assertThrows(IllegalArgumentException.class, () -> ReportType.parse(candidate));
    }
}
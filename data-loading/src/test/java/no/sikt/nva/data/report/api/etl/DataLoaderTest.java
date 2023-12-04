package no.sikt.nva.data.report.api.etl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

class DataLoaderTest {

    @Test
    void shouldLogStuff() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        new DataLoader();
        assertTrue(logAppender.getMessages().contains("Initializing DataLoader"));
    }
}

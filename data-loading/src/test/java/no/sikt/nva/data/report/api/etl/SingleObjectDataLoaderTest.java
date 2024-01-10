package no.sikt.nva.data.report.api.etl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.stubs.FakeContext;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SingleObjectDataLoaderTest {

    private Context context;

    @BeforeEach
    void setup() {
        context = new FakeContext();
    }

    @Test
    void shouldLogStuff() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        new SingleObjectDataLoader();
        assertTrue(logAppender.getMessages().contains("Initializing DataLoader"));
    }

    @Test
    void shouldReceiveEventBridgeEvent() {
        var event = new PersistedResourceEvent("someBucketName", "someKey", "someOperation");
        var handler = new SingleObjectDataLoader();
        assertDoesNotThrow(() -> handler.handleRequest(event, context));
    }
}

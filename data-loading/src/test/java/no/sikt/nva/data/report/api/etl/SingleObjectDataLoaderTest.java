package no.sikt.nva.data.report.api.etl;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.stubs.FakeContext;
import nva.commons.core.paths.UnixPath;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SingleObjectDataLoaderTest {

    public static final String BUCKET_NAME = "notRelevant";
    public static final String SOME_OPERATION = "someOperation";
    public static final String RESOURCES_FOLDER = "resources";
    public static final String NVI_CANDIDATES_FOLDER = "nvi-candidates";
    private Context context;

    private SingleObjectDataLoader handler;

    @BeforeEach
    void setup() {
        context = new FakeContext();
        handler = new SingleObjectDataLoader();
    }

    @Test
    void shouldLogStuff() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        new SingleObjectDataLoader();
        assertTrue(logAppender.getMessages().contains("Initializing DataLoader"));
    }

    @ParameterizedTest(name = "Should extract and log folderName {0}")
    @ValueSource(strings = {RESOURCES_FOLDER, NVI_CANDIDATES_FOLDER})
    void shouldExtractAndLogObjectParentFolder(String folderName) {
        var key = UnixPath.of(folderName, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, SOME_OPERATION);
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("Object folder: " + folderName));
    }

    @Test
    void shouldLogNoFolderIfKeyHasNoParent() {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        var key = randomString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, SOME_OPERATION);
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("No parent folder"));
    }

    @ParameterizedTest(name = "Should extract and log eventType type {0}")
    @ValueSource(strings = {"PutObject", "DeleteObject"})
    void shouldExtractAndLogOperationType(String eventType) {
        var key = UnixPath.of(RESOURCES_FOLDER, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, eventType);
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("Event type: " + EventType.parse(eventType)));
    }

    @Test
    void shouldLogUnknownEventTypeIfEventTypeIsUnknown() {
        var key = UnixPath.of(RESOURCES_FOLDER, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, randomString());
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("Unknown event type: " + event.eventType()));
    }
}

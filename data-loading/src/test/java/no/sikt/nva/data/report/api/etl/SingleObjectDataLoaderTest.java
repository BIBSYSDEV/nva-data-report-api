package no.sikt.nva.data.report.api.etl;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

    @ParameterizedTest(name = "should extract and log folderName {0}")
    @ValueSource(strings = {RESOURCES_FOLDER, NVI_CANDIDATES_FOLDER})
    void shouldExtractAndLogKeyPrefix(String folderName) {
        final var logAppender = LogUtils.getTestingAppenderForRootLogger();
        var key = UnixPath.of(folderName, randomString()).toString();
        var event = new PersistedResourceEvent(BUCKET_NAME, key, SOME_OPERATION);
        handler.handleRequest(event, context);
        assertTrue(logAppender.getMessages().contains("Object folder: " + folderName));
    }
}

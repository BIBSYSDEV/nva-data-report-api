package commons.db.utils;

import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DocumentUnwrapperTest {

    public static final Path NVI_CANDIDATE = Path.of("nvi_candidate_blob.json");
    public static final Path PUBLICATION = Path.of("publication_blob.json");
    public static final String CONTEXT_POINTER = "/@context";

    public static Stream<Named<String>> bodyProvider() {
        return Stream.of(
            Named.of("Publication", stringFromResources(PUBLICATION)),
            Named.of("NVI-candidate", stringFromResources(NVI_CANDIDATE))
        );
    }

    @ParameterizedTest
    @MethodSource("bodyProvider")
    void shouldUnwrapContext(String json) throws JsonProcessingException {
        var actual = DocumentUnwrapper.unwrap(json);
        assertTrue(actual.at(CONTEXT_POINTER).isObject());
    }
}
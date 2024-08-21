package commons.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DocumentTypeTest {

    public static Stream<Arguments> documentTypeProvider() {
        return Stream.of(Arguments.of("resources", DocumentType.PUBLICATION),
                         Arguments.of("nvi-candidates", DocumentType.NVI_CANDIDATE));
    }

    @ParameterizedTest
    @MethodSource("documentTypeProvider")
    void shouldParseDocumentTypeFromLocation(String location, DocumentType expected) {
        assertEquals(expected, DocumentType.fromLocation(location));
    }

    @Test
    void shouldThrowExceptionWhenParsingUnknownLocation() {
        var invalidLocation = "someLocation";
        assertThrows(IllegalArgumentException.class, () -> DocumentType.fromLocation(invalidLocation));
    }
}
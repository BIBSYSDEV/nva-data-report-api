package commons.db.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import java.util.stream.Stream;
import nva.commons.core.paths.UnixPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class GraphNameTest {

    public static final String GRAPH_NAME_TEMPLATE = "%s/%s/%s.nt";
    public static final String PATH_DELIMITER = "/";
    public static final char FILE_EXTENSION_DELIMITER = '.';

    public static Stream<Named<UnixPath>> s3UriProvider() {
        return Stream.of(
            Named.of("nva-candidate URI",
                     UnixPath.of("s3://localhost:8080/some-thing/candidate/1234.gz")),
            Named.of("nva-publication URI",
                     UnixPath.of("s3://localhost:8080/publication/4321.gz"))
        );
    }

    @ParameterizedTest
    @DisplayName("Should create named graph uri from s3 uri")
    @MethodSource("s3UriProvider")
    void shouldCreateGraphNameFromS3Uri(UnixPath unixPath) {
        var baseUri = URI.create("https://example.org");
        var graphName = GraphName.newBuilder()
                            .withBase(baseUri)
                            .fromUnixPath(unixPath)
                            .build()
                            .toUri();
        var expected = createExpected(baseUri, unixPath);
        assertEquals(expected, graphName.toString());
    }

    private String createExpected(URI baseUri, UnixPath uri) {
        var uriParts = uri.toString().split(PATH_DELIMITER);
        return String.format(GRAPH_NAME_TEMPLATE,
                             baseUri,
                             typePart(uriParts),
                             namePart(uriParts));
    }

    private String namePart(String[] uriParts) {
        var fileName = uriParts[uriParts.length - 1];
        return fileName.substring(0, fileName.lastIndexOf(FILE_EXTENSION_DELIMITER));
    }

    private String typePart(String[] uriParts) {
        return uriParts[uriParts.length - 2];
    }
}

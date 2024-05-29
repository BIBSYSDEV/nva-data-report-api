package no.sikt.nva.data.report.api.etl.transformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import java.net.URI;
import org.junit.jupiter.api.Test;

class NquadsTest {

    public static final URI GRAPH_NAME = URI.create("http://someGraphName.org");

    @Test
    void shouldWriteNquadsAsAscii() {
        var json = """
            {
                "@context": {
                    "someProperty": {
                        "@id": "http://someProperty.org",
                        "@type": "http://someType.org"
                    }
                },
                "someProperty": "\\u0000\\u0000\\u0000\\u0000 propertyValue"
            }
            """;
        var result = Nquads.transform(json, GRAPH_NAME).toString();
        assertFalse(result.contains("\u0000"));
    }
}
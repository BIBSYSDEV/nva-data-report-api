package no.sikt.nva.data.report.api.etl.transformer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import java.net.URI;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
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
        var model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, IoUtils.stringToStream(json), Lang.JSONLD);
        var result = Nquads.transform(GRAPH_NAME, model).toString();
        assertFalse(result.contains("\u0000"));
    }
}
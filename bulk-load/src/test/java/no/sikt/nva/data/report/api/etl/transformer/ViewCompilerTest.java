package no.sikt.nva.data.report.api.etl.transformer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Test;

class ViewCompilerTest {

    private static final String JOURNAL_JSON = "journal.json";
    private static final String JOURNAL_NT = "journal.nt";

    @Test
    void shouldReduceTriplesToViewRequiredToProduceApiData() {
        var inputStream = IoUtils.inputStreamFromResources(JOURNAL_JSON);
        var model = new ViewCompiler(inputStream).extractView();
        assertTrue(expected().isIsomorphicWith(model));
    }

    private static Model expected() {
        var triples = IoUtils.inputStreamFromResources(JOURNAL_NT);
        var model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, triples, Lang.NTRIPLES);
        return model;
    }
}

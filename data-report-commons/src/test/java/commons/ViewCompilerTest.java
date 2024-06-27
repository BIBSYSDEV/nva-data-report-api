package commons;

import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ViewCompilerTest {

    private static final String ACADEMIC_ARTICLE_JSON = "academicArticle.json";
    private static final String ACADEMIC_ARTICLE_NT = "academicArticle.nt";

    @Test
    void shouldReduceTriplesToViewRequiredToProduceApiData() {
        var inputStream = IoUtils.inputStreamFromResources(ACADEMIC_ARTICLE_JSON);
        var model = new ViewCompiler(inputStream).extractView();
        Assertions.assertTrue(expected().isIsomorphicWith(model));
    }

    private static Model expected() {
        var triples = IoUtils.inputStreamFromResources(ACADEMIC_ARTICLE_NT);
        var model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, triples, Lang.NTRIPLES);
        return model;
    }
}

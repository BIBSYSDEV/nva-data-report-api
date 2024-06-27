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
    private static final String NVI_CANDIDATE = "nviCandidate.json";
    private static final String NVI_CANDIDATE_NT = "nviCandidate.nt";

    @Test
    void shouldReduceTriplesToPublicationViewRequiredToProduceApiData() {
        var inputStream = IoUtils.inputStreamFromResources(ACADEMIC_ARTICLE_JSON);
        var model = new ViewCompiler(inputStream).extractPublicationView();
        Assertions.assertTrue(expected().isIsomorphicWith(model));
    }

    @Test
    void shouldReduceTriplesToNviCandidateViewRequiredToProduceApiData() {
        var inputStream = IoUtils.inputStreamFromResources(ACADEMIC_ARTICLE_JSON);
        var model = new ViewCompiler(inputStream).extractNviCandidateView();
        Assertions.assertTrue(expected().isIsomorphicWith(model));
    }

    private static Model expected() {
        var triples = IoUtils.inputStreamFromResources(ACADEMIC_ARTICLE_NT);
        var model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, triples, Lang.NTRIPLES);
        return model;
    }
}

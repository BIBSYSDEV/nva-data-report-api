package commons;

import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static nva.commons.core.ioutils.IoUtils.stringToStream;
import java.net.URI;
import java.nio.file.Path;
import no.sikt.nva.data.report.testing.utils.StaticTestDataUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ViewCompilerTest {

    private static final Path ACADEMIC_ARTICLE_NT = Path.of("academicArticle.nt");
    private static final Path NVI_CANDIDATE_NT = Path.of("nviCandidate.nt");

    @Test
    void shouldReduceTriplesToPublicationViewRequiredToProduceApiData() {
        var uri = randomUri();
        var inputStream = StaticTestDataUtil.getPublication(uri);
        var model = new ViewCompiler(inputStream).extractView();
        Assertions.assertTrue(expected(ACADEMIC_ARTICLE_NT, uri).isIsomorphicWith(model));
    }

    @Test
    void shouldReduceTriplesToNviCandidateViewRequiredToProduceApiData() {
        var uri = randomUri();
        var inputStream = StaticTestDataUtil.getNviCandidate(uri);
        var model = new ViewCompiler(inputStream).extractView();
        Assertions.assertTrue(expected(NVI_CANDIDATE_NT, uri).isIsomorphicWith(model));
    }

    private static Model expected(Path tripleFile, URI uri) {
        var triples = stringToStream(stringFromResources(tripleFile)
                                         .replace("__REPLACE_ID__", uri.toString()));
        var model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, triples, Lang.NTRIPLES);
        return model;
    }
}

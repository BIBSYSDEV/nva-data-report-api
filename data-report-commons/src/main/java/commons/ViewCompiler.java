package commons;

import java.io.InputStream;
import java.nio.file.Path;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class ViewCompiler {

    public static final Path NVA_QUERY = Path.of("view_of_publication.sparql");
    public static final Path NVI_QUERY = Path.of("view_of_nvi_candidate.sparql");

    private final Model model;

    public ViewCompiler(InputStream inputStream) {
        this.model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, inputStream, Lang.JSONLD);
    }

    public Model extractPublicationView() {
        var query = IoUtils.stringFromResources(NVA_QUERY);
        try (var queryExecution = QueryExecutionFactory.create(query, model)) {
            return queryExecution.execConstruct();
        }
    }

    public Model extractNviCandidateView() {
        var query = IoUtils.stringFromResources(NVI_QUERY);
        try (var queryExecution = QueryExecutionFactory.create(query, model)) {
            return queryExecution.execConstruct();
        }
    }
}

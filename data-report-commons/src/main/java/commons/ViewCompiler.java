package commons;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

public class ViewCompiler {

    public static final Path NVA_QUERY = Path.of("view_of_publication.sparql");
    public static final Path NVI_QUERY = Path.of("view_of_nvi_candidate.sparql");
    public static final String PUBLICATION = "https://nva.sikt.no/ontology/publication#Publication";
    public static final String NVI_CANDIDATE = "https://nva.sikt.no/ontology/publication#NviCandidate";

    private final Model model;

    public ViewCompiler(InputStream inputStream) {
        this.model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, inputStream, Lang.JSONLD);
    }

    public ViewCompiler(Model model) {
        this.model = model;
    }

    public Model extractView(URI id) {
        if (isPublication(model, id)) {
            return extractPublicationView();
        } else if (isNviCandidate(model, id)) {
            return extractNviCandidateView();
        } else {
            return model;
        }
    }

    private static boolean isNotObject(Model model, URI id) {
        var resource = model.createResource(id.toString());
        var statementsWithIdAsObject = model.listStatements(null, null, resource);
        return !statementsWithIdAsObject.hasNext();
    }

    private Model extractNviCandidateView() {
        var query = IoUtils.stringFromResources(NVI_QUERY);
        try (var queryExecution = QueryExecutionFactory.create(query, model)) {
            return queryExecution.execConstruct();
        }
    }

    private Model extractPublicationView() {
        var query = IoUtils.stringFromResources(NVA_QUERY);
        try (var queryExecution = QueryExecutionFactory.create(query, model)) {
            return queryExecution.execConstruct();
        }
    }

    private boolean isPublication(Model model, URI id) {
        var publicationType = model.createResource(PUBLICATION);
        return model.contains(model.createResource(id.toString()), RDF.type, publicationType) && isNotObject(model, id);
    }

    private boolean isNviCandidate(Model model, URI id) {
        var publicationType = model.createResource(NVI_CANDIDATE);
        return model.contains(model.createResource(id.toString()), RDF.type, publicationType) && isNotObject(model, id);
    }
}

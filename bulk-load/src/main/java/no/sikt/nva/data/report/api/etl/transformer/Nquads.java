package no.sikt.nva.data.report.api.etl.transformer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public final class Nquads {

    private static final String PATH_SEPARATOR = "/";
    private final DatasetGraph graph;

    private Nquads(DatasetGraph graph) {
        this.graph = graph;
    }

    public static Nquads transform(File file, URI graphName) throws IOException {
        var model = loadModel(file);
        var node = model.createResource(graphName.toString() + PATH_SEPARATOR + file.getName()).asNode();
        var graph = DatasetGraphFactory.createTxnMem();
        graph.addGraph(node, model.getGraph());
        return new Nquads(graph);
    }

    @Override
    public String toString() {
        var stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, graph, RDFFormat.NQUADS);
        return stringWriter.toString();
    }

    private static Model loadModel(File file) throws IOException {
        var data = new ByteArrayInputStream(Files.readAllBytes(file.toPath()));
        var model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, data, Lang.NTRIPLES);
        return model;
    }
}

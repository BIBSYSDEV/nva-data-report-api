package no.sikt.nva.data.report.api.etl.transformer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public final class Nquads {
    private final DatasetGraph graph;
    private Nquads(DatasetGraph graph) {
        this.graph = graph;
    }

    public static Nquads transform(String data, URI graphName) {
        var model = loadModel(data);
        var node = model.createResource(graphName.toString()).asNode();
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

    private static Model loadModel(String data) {
        var model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, IoUtils.stringToStream(data), Lang.JSONLD);
        return model;
    }
}

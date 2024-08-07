package no.sikt.nva.data.report.api.etl.transformer;

import java.io.StringWriter;
import java.net.URI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public final class Nquads {

    private final DatasetGraph graph;

    private Nquads(DatasetGraph graph) {
        this.graph = graph;
    }

    public static Nquads transform(URI graphName, Model model) {
        var node = model.createResource(graphName.toString()).asNode();
        var graph = DatasetGraphFactory.createTxnMem();
        graph.addGraph(node, model.getGraph());
        return new Nquads(graph);
    }

    @Override
    public String toString() {
        var stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, graph, RDFFormat.NQUADS_ASCII);
        return stringWriter.toString();
    }
}

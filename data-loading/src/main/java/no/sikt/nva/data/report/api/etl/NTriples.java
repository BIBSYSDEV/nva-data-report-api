package no.sikt.nva.data.report.api.etl;

import java.io.StringWriter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

public class NTriples {

    private final Model model;

    private NTriples(Model model) {
        this.model = model;
    }

    public static NTriples transform(Model model) {
        return new NTriples(model);
    }

    @Override
    public String toString() {
        var stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, model, RDFFormat.NTRIPLES_ASCII);
        return stringWriter.toString();
    }
}

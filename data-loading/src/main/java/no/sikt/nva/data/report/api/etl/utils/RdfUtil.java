package no.sikt.nva.data.report.api.etl.utils;

import java.io.StringWriter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public final class RdfUtil {

    private RdfUtil() {
    }

    public static String toNTriples(Model model) {
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, model, Lang.NTRIPLES);
        return stringWriter.toString();
    }
}
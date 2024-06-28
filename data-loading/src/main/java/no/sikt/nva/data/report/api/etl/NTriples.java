package no.sikt.nva.data.report.api.etl;

import static java.util.Objects.isNull;
import java.io.InputStream;
import java.io.StringWriter;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;

public class NTriples {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(NTriples.class);
    private final Model model;

    private NTriples(Model model) {
        this.model = model;
    }

    public static NTriples transform(String data) {
        var model = loadModel(data);
        return new NTriples(model);
    }

    @Override
    public String toString() {
        var stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, model, RDFFormat.NTRIPLES_ASCII);
        return stringWriter.toString();
    }

    private static Model loadModel(String data) {
        var model = ModelFactory.createDefaultModel();
        loadDataIntoModel(model, IoUtils.stringToStream(data));
        return model;
    }

    @JacocoGenerated
    private static void loadDataIntoModel(Model model, InputStream inputStream) {
        if (isNull(inputStream)) {
            return;
        }
        try {
            RDFDataMgr.read(model, inputStream, Lang.JSONLD);
        } catch (RiotException e) {
            logInvalidJsonLdInput(e);
        }
    }

    @JacocoGenerated
    private static void logInvalidJsonLdInput(Exception exception) {
        LOGGER.error("Invalid JSON LD input encountered: ", exception);
    }
}

package no.sikt.nva.data.report.api.fetch.testutils.generator.model.nvi;

import java.util.UUID;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public class BlankNodeUtil {

    public static final String BLANK_NODE_PREFIX = "_:";
    public static final String HYPHEN = "-";
    public static final String EMPTY_STRING = "";

    public static Resource createRandom(Model model) {
        return model.createResource(BLANK_NODE_PREFIX + UUID.randomUUID().toString().replace(HYPHEN, EMPTY_STRING));
    }
}

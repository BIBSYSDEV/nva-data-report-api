package no.sikt.nva.data.report.api.etl.utils;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Path;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;

public final class DocumentUnwrapper {

    public static final String JSON_PTR_BODY = "/body";
    public static final String CONTEXT_NODE = "@context";
    public static final String JSON_PTR_CONTEXT = "/@context";
    public static final String SCIENTIFIC_INDEX = "scientific-index";
    public static final String NVI_CONTEXT_JSONLD = "nvi_context.jsonld";
    public static final String NVA_CONTEXT_JSONLD = "nva_context.jsonld";
    public static final String API_HOST_PLACEHOLDER = "__API_HOST__";
    public static final String API_HOST = "API_HOST";

    private DocumentUnwrapper() {
    }

    public static String unwrap(String indexDocument) throws JsonProcessingException {
        var objectNode = dtoObjectMapper.readTree(indexDocument);
        var jsonld = objectNode.at(JSON_PTR_BODY);
        var context = getReplacementContext(jsonld);
        ((ObjectNode) jsonld).set(CONTEXT_NODE, context.at(JSON_PTR_CONTEXT));
        return jsonld.toString();
    }

    private static JsonNode getReplacementContext(JsonNode jsonld) throws JsonProcessingException {
        var originalContext = jsonld.at(JSON_PTR_CONTEXT).asText();
        var contextFile = originalContext.contains(SCIENTIFIC_INDEX)
                              ? NVI_CONTEXT_JSONLD
                              : NVA_CONTEXT_JSONLD;
        return dtoObjectMapper.readTree(getContext(contextFile));
    }

    private static String getContext(String contextFile) {
        return IoUtils.stringFromResources(Path.of(contextFile))
                   .replace(API_HOST_PLACEHOLDER, new Environment().readEnv(API_HOST));
    }
}

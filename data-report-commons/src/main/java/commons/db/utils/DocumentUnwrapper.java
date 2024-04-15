package commons.db.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Path;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.ioutils.IoUtils;

public final class DocumentUnwrapper {

    public static final String JSON_PTR_BODY = "/body";
    public static final String CONTEXT_NODE = "@context";
    public static final String JSON_PTR_CONTEXT = "/@context";
    public static final String SCIENTIFIC_INDEX = "scientific-index";
    public static final String NVI_CONTEXT_JSONLD = "nvi_context.jsonld";
    public static final String NVA_CONTEXT_JSONLD = "nva_context.jsonld";
    private final String apiDomain;

    public DocumentUnwrapper(String apiDomain) {
        this.apiDomain = apiDomain;
    }

    public JsonNode unwrap(String indexDocument) throws JsonProcessingException {
        var objectNode = JsonUtils.dtoObjectMapper.readTree(indexDocument);
        var jsonld = objectNode.at(JSON_PTR_BODY);
        var context = getReplacementContext(jsonld);
        ((ObjectNode) jsonld).set(CONTEXT_NODE, context.at(JSON_PTR_CONTEXT));
        return jsonld;
    }

    private JsonNode getReplacementContext(JsonNode jsonld) throws JsonProcessingException {
        var originalContext = jsonld.at(JSON_PTR_CONTEXT).asText();
        var contextFile = originalContext.contains(SCIENTIFIC_INDEX)
                              ? NVI_CONTEXT_JSONLD
                              : NVA_CONTEXT_JSONLD;
        return JsonUtils.dtoObjectMapper.readTree(getContext(contextFile));
    }

    private String getContext(String contextFile) {
        return IoUtils.stringFromResources(Path.of(contextFile)).replace("__REPLACE_WITH_API_DOMAIN__", apiDomain);
    }
}

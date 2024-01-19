package no.sikt.nva.data.report.api.etl.utils;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public final class DocumentUnwrapper {

    public static final String JSON_PTR_BODY = "/body";

    private DocumentUnwrapper() {
    }

    public static String unwrap(String indexDocument) throws JsonProcessingException {
        var objectNode = dtoObjectMapper.readTree(indexDocument);
        return objectNode.at(JSON_PTR_BODY).toString();
    }
}

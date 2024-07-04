package no.sikt.nva.data.report.testing.utils;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static nva.commons.core.ioutils.IoUtils.stringToStream;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;

public class ViewCompilerTestUtils {

    private static final Path ACADEMIC_ARTICLE_JSON = Path.of("academicArticle.json");
    private static final Path NVI_CANDIDATE = Path.of("nviCandidate.json");

    public static InputStream getPublication(URI id) {
        return stringToStream(fromResourcesReplacingId(ACADEMIC_ARTICLE_JSON, id));
    }

    public static JsonNode getPublicationJsonNode(URI id) {
        return attempt(() -> dtoObjectMapper.readTree(getPublication(id))).orElseThrow();
    }

    public static JsonNode getNviCandidateJsonNode(URI id) {
        return attempt(() -> dtoObjectMapper.readTree(getNviCandidate(id))).orElseThrow();
    }

    public static InputStream getNviCandidate(URI id) {
        return stringToStream(fromResourcesReplacingId(NVI_CANDIDATE, id));
    }

    private static String fromResourcesReplacingId(Path path, URI id) {
        return stringFromResources(path).replace("__REPLACE_ID__", id.toString());
    }
}

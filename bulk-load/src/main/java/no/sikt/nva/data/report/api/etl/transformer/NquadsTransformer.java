package no.sikt.nva.data.report.api.etl.transformer;

import static commons.utils.GzipUtil.compress;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.ioutils.IoUtils.stringToStream;
import com.fasterxml.jackson.databind.JsonNode;
import commons.ViewCompiler;
import commons.handlers.BulkTransformerHandler;
import commons.model.ContentWithLocation;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class NquadsTransformer extends BulkTransformerHandler {

    private static final Logger logger = LoggerFactory.getLogger(NquadsTransformer.class);
    private static final Environment ENVIRONMENT = new Environment();
    private static final String NULL_CHARACTER = "\\u0000";
    private static final String LOADER_BUCKET = "LOADER_BUCKET";
    private static final String NQUADS_GZIPPED = ".nquads.gz";
    private static final String ID_POINTER = "/id";
    private static final String NT_EXTENSION = ".nt";
    private static final String MISSING_ID_NODE_IN_CONTENT_ERROR = "Missing id-node in content: {}";
    private final S3Client s3OutputClient;

    @JacocoGenerated
    public NquadsTransformer() {
        this(defaultS3Client(), defaultS3Client(), defaultS3Client(), defaultEventBridgeClient());
    }

    public NquadsTransformer(S3Client s3ResourcesClient,
                             S3Client s3BatchesClient,
                             S3Client s3OutputClient,
                             EventBridgeClient eventBridgeClient) {
        super(s3ResourcesClient, s3BatchesClient, eventBridgeClient);
        this.s3OutputClient = s3OutputClient;
    }

    @Override
    protected List<ContentWithLocation> processBatch(Stream<JsonNode> jsonNodeStream, String batchLocation) {
        var nquads = jsonNodeStream.map(this::mapToNquads)
                         .collect(Collectors.joining(System.lineSeparator()));
        return List.of(new ContentWithLocation(UnixPath.ROOT_PATH, nquads));
    }

    @Override
    protected void persist(List<ContentWithLocation> transformedData) {
        transformedData.forEach(this::persist);
    }

    private static PutObjectRequest buildRequest(ContentWithLocation transformedData) {
        var key = generateKey(transformedData);
        return PutObjectRequest.builder()
                   .bucket(ENVIRONMENT.readEnv(LOADER_BUCKET))
                   .key(key)
                   .build();
    }

    private static String generateKey(ContentWithLocation transformedData) {
        return transformedData.location().equals(UnixPath.ROOT_PATH)
                   ? UUID.randomUUID() + NQUADS_GZIPPED
                   : transformedData.location().addChild(UUID.randomUUID().toString()) + NQUADS_GZIPPED;
    }

    @JacocoGenerated
    private static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
    }

    @JacocoGenerated
    private static EventBridgeClient defaultEventBridgeClient() {
        return EventBridgeClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
    }

    private static URI getId(JsonNode content) {
        var id = content.at(ID_POINTER);
        if (id.isMissingNode()) {
            logger.error(MISSING_ID_NODE_IN_CONTENT_ERROR, content);
            throw new MissingIdException();
        }
        return URI.create(id.textValue());
    }

    private void persist(ContentWithLocation transformation) {
        var request = buildRequest(transformation);
        compressAndPersist(transformation, request);
    }

    private void compressAndPersist(ContentWithLocation transformedData, PutObjectRequest request) {
        var data = attempt(() -> compress(transformedData.content())).orElseThrow();
        s3OutputClient.putObject(request, RequestBody.fromBytes(data));
    }

    // Necessary to avoid issues with Neptune downstream
    // https://docs.aws.amazon.com/neptune/latest/userguide/limits.html#limits-nulls
    private String removeNullCharacters(String nquads) {
        return nquads.replace(NULL_CHARACTER, EMPTY_STRING);
    }

    private String mapToNquads(JsonNode content) {
        var id = getId(content);
        var model = applyView(content, id);
        return removeNullCharacters(Nquads.transform(URI.create(id + NT_EXTENSION), model).toString());
    }

    private Model applyView(JsonNode content, URI id) {
        return new ViewCompiler(stringToStream(content.toString())).extractView(id);
    }
}


package no.sikt.nva.data.report.api.export;

import static commons.utils.GzipUtil.compress;
import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.ioutils.IoUtils.stringToStream;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import commons.ViewCompiler;
import commons.db.utils.DocumentUnwrapper;
import commons.formatter.CsvFormatter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class CsvBulkTransformer extends EventHandler<KeyBatchEvent, Void> {

    public static final String TEMPLATE_DIRECTORY = "template";
    public static final String SPARQL = ".sparql";
    public static final String PUBLICATION = "publication";
    public static final String API_HOST = new Environment().readEnv("API_HOST");
    private static final String LINE_BREAK = "\n";
    private final S3Client s3BatchesClient;
    private final S3Client s3OutputClient;
    private final S3Client s3ResourcesClient;
    private String keyBatchesBucket = "keyBatchesBucket";
    private String exportBucket = "exportBucket";
    private String expandedResourceBucket = "expandedResourceBucket";

    @JacocoGenerated
    public CsvBulkTransformer() {
        this(defaultS3Client(), defaultS3Client(), defaultS3Client());
    }

    public CsvBulkTransformer(S3Client s3BatchesClient, S3Client s3OutputClient, S3Client s3ResourcesClient) {
        super(KeyBatchEvent.class);
        this.s3BatchesClient = s3BatchesClient;
        this.s3OutputClient = s3OutputClient;
        this.s3ResourcesClient = s3ResourcesClient;
    }

    @Override
    protected Void processInput(KeyBatchEvent keyBatchEvent, AwsEventBridgeEvent<KeyBatchEvent> event,
                                Context context) {
        var batchResponse = fetchSingleBatch();

        batchResponse.getKey()
            .map(this::extractContent)
            .filter(keys -> !keys.isEmpty())
            .map(this::mapToIndexDocuments)
            .map(this::mapToCsv)
            .map(content -> attempt(() -> compress(content)).orElseThrow())
            .map(this::persist);

        return null;
    }

    @JacocoGenerated
    private static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
    }

    private static Path constructPath(String sparqlTemplate) {
        return Path.of(TEMPLATE_DIRECTORY, sparqlTemplate + SPARQL);
    }

    private Stream<String> extractIdentifiers(String value) {
        return nonNull(value) && !value.isBlank()
                   ? Arrays.stream(value.split(LINE_BREAK))
                   : Stream.empty();
    }

    private Stream<JsonNode> mapToIndexDocuments(String content) {
        return extractIdentifiers(content)
                   .filter(Objects::nonNull)
                   .map(this::fetchS3Content)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .map(this::unwrap);
    }

    private JsonNode unwrap(String json) {
        return attempt(() -> new DocumentUnwrapper(API_HOST).unwrap(json)).orElseThrow();
    }

    private Optional<String> fetchS3Content(String key) {
        var s3Driver = new S3Driver(s3ResourcesClient, expandedResourceBucket);
        try {
            return Optional.of(s3Driver.getFile(UnixPath.of(key)));
        } catch (NoSuchKeyException noSuchKeyException) {
            return Optional.empty();
        }
    }

    private boolean persist(byte[] content) {
        var request = PutObjectRequest.builder()
                          .bucket(exportBucket)
                          .key(UUID.randomUUID() + ".gz")
                          .build();
        var response = s3OutputClient.putObject(request, RequestBody.fromBytes(content));
        return response.sdkHttpResponse().isSuccessful();
    }

    private String mapToCsv(Stream<JsonNode> elements) {
        var model = ModelFactory.createDefaultModel();
        elements.forEach(element -> RDFDataMgr.read(model, stringToStream(element.toString()), Lang.JSONLD));
        var query = getQuery(PUBLICATION);
        try (var queryExecution = QueryExecutionFactory.create(query, model)) {
            var resultSet = queryExecution.execSelect();
            return new CsvFormatter().format(resultSet);
        }
    }

    private Query getQuery(String sparqlTemplate) {
        var template = constructPath(sparqlTemplate);
        var sparqlString = IoUtils.stringFromResources(template);
        return QueryFactory.create(sparqlString);
    }

    private String extractContent(String key) {
        var s3Driver = new S3Driver(s3BatchesClient, keyBatchesBucket);
        return attempt(() -> s3Driver.getFile(UnixPath.of(key))).orElseThrow();
    }

    private ListingResponse fetchSingleBatch() {
        var response = s3BatchesClient.listObjectsV2(
            ListObjectsV2Request.builder()
                .bucket(keyBatchesBucket)
                .maxKeys(1)
                .build());
        return new ListingResponse(response);
    }

    private static class ListingResponse {

        private final String key;

        public ListingResponse(ListObjectsV2Response response) {
            this.key = extractKey(response);
        }

        public Optional<String> getKey() {
            return Optional.ofNullable(key);
        }

        private static String extractKey(ListObjectsV2Response response) {
            var contents = response.contents();
            return contents.isEmpty() ? null : contents.getFirst().key();
        }
    }
}

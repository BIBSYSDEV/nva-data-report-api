package no.sikt.nva.data.report.api.export;

import static nva.commons.core.ioutils.IoUtils.stringToStream;
import com.fasterxml.jackson.databind.JsonNode;
import commons.formatter.CsvFormatter;
import commons.handlers.BulkTransformerHandler;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class CsvTransformer extends BulkTransformerHandler {

    private static final String TEMPLATE_DIRECTORY = "template";
    private static final String SPARQL = ".sparql";
    private static final String PUBLICATION = "publication";
    private static final String ENV_VAR_EXPORT_BUCKET = "EXPORT_BUCKET";
    private final S3Client s3OutputClient;
    private final String exportBucket;

    @JacocoGenerated
    public CsvTransformer() {
        this(defaultS3Client(), defaultS3Client(), defaultS3Client(), defaultEventBridgeClient());
    }

    public CsvTransformer(S3Client s3BatchesClient, S3Client s3ResourcesClient, S3Client s3OutputClient,
                          EventBridgeClient eventBridgeClient) {
        super(s3ResourcesClient, s3BatchesClient, eventBridgeClient);
        this.exportBucket = new Environment().readEnv(ENV_VAR_EXPORT_BUCKET);
        this.s3OutputClient = s3OutputClient;
    }

    @Override
    protected String processBatch(Stream<JsonNode> jsonNodeStream) {
        var model = ModelFactory.createDefaultModel();
        jsonNodeStream.forEach(jsonNode -> RDFDataMgr.read(model, stringToStream(jsonNode.toString()), Lang.JSONLD));
        var query = getQuery();
        try (var queryExecution = QueryExecutionFactory.create(query, model)) {
            var resultSet = queryExecution.execSelect();
            return new CsvFormatter().format(resultSet);
        }
    }

    @Override
    protected boolean persist(byte[] content) {
        var request = PutObjectRequest.builder()
                          .bucket(exportBucket)
                          .key(PUBLICATION + UUID.randomUUID() + ".gz")
                          .build();
        var response = s3OutputClient.putObject(request, RequestBody.fromBytes(content));
        return response.sdkHttpResponse().isSuccessful();
    }

    @JacocoGenerated
    private static EventBridgeClient defaultEventBridgeClient() {
        return EventBridgeClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
    }

    @JacocoGenerated
    private static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
    }

    private static Path constructPath(String sparqlTemplate) {
        return Path.of(TEMPLATE_DIRECTORY, sparqlTemplate + SPARQL);
    }

    private Query getQuery() {
        var template = constructPath(PUBLICATION);
        var sparqlString = IoUtils.stringFromResources(template);
        return QueryFactory.create(sparqlString);
    }
}

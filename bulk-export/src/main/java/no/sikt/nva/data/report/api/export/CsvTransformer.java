package no.sikt.nva.data.report.api.export;

import static nva.commons.core.ioutils.IoUtils.stringToStream;
import com.fasterxml.jackson.databind.JsonNode;
import commons.formatter.CsvFormatter;
import commons.handlers.BulkTransformerHandler;
import commons.model.ContentWithLocation;
import commons.model.ReportType;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class CsvTransformer extends BulkTransformerHandler {

    private static final String PERSISTED_RESOURCES_PUBLICATION_PREFIX = "resources";
    private static final String PERSISTED_RESOURCES_NVI_PREFIX = "nvi-candidates";
    private static final String DELIMITER = "/";
    private static final String TEMPLATE_DIRECTORY = "template";
    private static final String SPARQL = ".sparql";
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
    protected List<ContentWithLocation> processBatch(Stream<JsonNode> jsonNodeStream, String batchLocation) {
        var model = ModelFactory.createDefaultModel();
        jsonNodeStream.forEach(jsonNode -> RDFDataMgr.read(model, stringToStream(jsonNode.toString()), Lang.JSONLD));
        if (PERSISTED_RESOURCES_PUBLICATION_PREFIX.equals(batchLocation)) {
            return transformResources(model);
        } else if (PERSISTED_RESOURCES_NVI_PREFIX.equals(batchLocation)) {
            return List.of(transform(model, ReportType.NVI));
        } else {
            throw new IllegalArgumentException(
                "Unknown batch location provided. Valid key batch locations are 'resources' and 'nvi-candidates'");
        }
    }

    @Override
    protected void persist(List<ContentWithLocation> transformedData) {
        transformedData.forEach(this::persist);
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

    private List<ContentWithLocation> transformResources(Model model) {
        return ReportType.getAllTypesExcludingNviReport()
                   .stream()
                   .map(reportType -> transform(model, reportType))
                   .toList();
    }

    private void persist(ContentWithLocation transformation) {
        var request = buildRequest(transformation.location());
        s3OutputClient.putObject(request, RequestBody.fromString(transformation.content()));
    }

    private ContentWithLocation transform(Model model, ReportType reportType) {
        var query = getQuery(reportType);
        try (var queryExecution = QueryExecutionFactory.create(query, model)) {
            var resultSet = queryExecution.execSelect();
            return new ContentWithLocation(UnixPath.of(reportType.getType()), new CsvFormatter().format(resultSet));
        }
    }

    private Query getQuery(ReportType reportType) {
        return QueryFactory.create(generateQuery(reportType));
    }

    private String generateQuery(ReportType reportType) {
        var template = constructPath(reportType.getType());
        return IoUtils.stringFromResources(template);
    }

    private PutObjectRequest buildRequest(UnixPath path) {
        return PutObjectRequest.builder()
                   .bucket(exportBucket)
                   .key(path + DELIMITER + UUID.randomUUID())
                   .build();
    }
}

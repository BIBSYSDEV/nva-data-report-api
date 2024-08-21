package no.sikt.nva.data.report.api.export;

import static nva.commons.core.ioutils.IoUtils.stringToStream;
import com.fasterxml.jackson.databind.JsonNode;
import commons.formatter.CsvFormatter;
import commons.handlers.BulkTransformerHandler;
import commons.model.ContentWithLocation;
import commons.model.ReportType;
import commons.service.ModelQueryService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;
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

    private static final String ENCODING = StandardCharsets.UTF_8.name();

    private static final String CONTENT_TYPE = "text/csv; charset=" + ENCODING;
    private static final String DELIMITER = "/";
    private static final String FILE_EXTENSION_CSV = ".csv";
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
        var model = createModelAndLoadInput(jsonNodeStream);
        var documentType = DocumentType.fromLocation(batchLocation);
        return switch (documentType) {
            case PUBLICATION -> transformResources(model);
            case NVI_CANDIDATE -> List.of(transform(model, ReportType.NVI));
        };
    }

    @Override
    protected void persist(List<ContentWithLocation> transformedData) {
        transformedData.forEach(this::persist);
    }

    private static Model createModelAndLoadInput(Stream<JsonNode> jsonNodeStream) {
        var model = ModelFactory.createDefaultModel();
        jsonNodeStream.forEach(jsonNode -> RDFDataMgr.read(model, stringToStream(jsonNode.toString()), Lang.JSONLD));
        return model;
    }

    @JacocoGenerated
    private static EventBridgeClient defaultEventBridgeClient() {
        return EventBridgeClient.builder().httpClient(UrlConnectionHttpClient.create()).build();
    }

    @JacocoGenerated
    private static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
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
        var resultSet = new ModelQueryService().query(model, reportType);
        var formatted = new CsvFormatter().format(resultSet);
        return new ContentWithLocation(UnixPath.of(reportType.getType()), formatted);
    }

    private PutObjectRequest buildRequest(UnixPath path) {
        return PutObjectRequest.builder()
                   .bucket(exportBucket)
                   .key(path + DELIMITER + UUID.randomUUID() + FILE_EXTENSION_CSV)
                   .contentEncoding(ENCODING)
                   .contentType(CONTENT_TYPE)
                   .build();
    }
}

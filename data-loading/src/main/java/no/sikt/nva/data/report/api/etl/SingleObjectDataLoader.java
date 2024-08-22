package no.sikt.nva.data.report.api.etl;

import static commons.model.DocumentType.NVI_CANDIDATE;
import static no.sikt.nva.data.report.api.etl.model.EventType.UPSERT;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import commons.StorageReader;
import commons.StorageWriter;
import commons.db.utils.DocumentUnwrapper;
import commons.formatter.CsvFormatter;
import commons.model.ContentWithLocation;
import commons.model.DocumentType;
import commons.model.ReportType;
import commons.service.ModelQueryService;
import java.time.LocalDateTime;
import java.util.List;
import no.sikt.nva.data.report.api.etl.aws.S3StorageReader;
import no.sikt.nva.data.report.api.etl.aws.S3StorageWriter;
import no.sikt.nva.data.report.api.etl.model.EventType;
import no.sikt.nva.data.report.api.etl.model.PersistedResourceEvent;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UnixPath;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleObjectDataLoader implements RequestHandler<PersistedResourceEvent, Void> {

    public static final Logger LOGGER = LoggerFactory.getLogger(SingleObjectDataLoader.class);
    public static final String EXPANDED_RESOURCES_BUCKET = "EXPANDED_RESOURCES_BUCKET";
    public static final String API_HOST = "API_HOST";
    public static final String EXPORT_BUCKET = "EXPORT_BUCKET";
    public static final String HYPHEN = "-";
    public static final String IDENTIFIER = "identifier";
    private final StorageReader<UnixPath> storageReader;
    private final StorageWriter storageWriter;

    @JacocoGenerated
    public SingleObjectDataLoader() {
        this(new S3StorageReader(new Environment().readEnv(EXPANDED_RESOURCES_BUCKET)),
             new S3StorageWriter(new Environment().readEnv(EXPORT_BUCKET)));
    }

    public SingleObjectDataLoader(StorageReader<UnixPath> storageReader, StorageWriter storageWriter) {
        LOGGER.info("Initializing SingleObjectDataLoader");
        this.storageReader = storageReader;
        this.storageWriter = storageWriter;
    }

    @Override
    public Void handleRequest(PersistedResourceEvent input, Context context) {
        input.validate();
        logInput(input);
        var eventType = EventType.parse(input.eventType());
        var documentType = DocumentType.fromLocation(input.getLocation());
        if (UPSERT.equals(eventType)) {
            transformAndPersistObject(documentType, UnixPath.of(input.key()));
        }
        return null;
    }

    private static JsonNode toJsonNode(String blob) {
        var documentUnwrapper = new DocumentUnwrapper(new Environment().readEnv(API_HOST));
        return attempt(() -> documentUnwrapper.unwrap(blob)).orElseThrow();
    }

    private static UnixPath constructNewLocation(String folder, String identifier) {
        return UnixPath.of(folder).addChild(identifier + HYPHEN + LocalDateTime.now());
    }

    private static ContentWithLocation transform(Model model, ReportType reportType, String identifier) {
        var result = new ModelQueryService().query(model, reportType);
        var formatted = new CsvFormatter().format(result);
        return new ContentWithLocation(constructNewLocation(reportType.getType(), identifier), formatted);
    }

    private static Model loadIntoModel(JsonNode resource) {
        var model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, IoUtils.stringToStream(resource.toString()), Lang.JSONLD);
        return model;
    }

    private void transformAndPersistObject(DocumentType documentType, UnixPath objectKey) {
        var resource = readAsJsonNode(objectKey);
        var identifier = resource.get(IDENTIFIER).asText();
        var model = loadIntoModel(resource);
        var csvContent = NVI_CANDIDATE.equals(documentType)
                             ? List.of(transform(model, ReportType.NVI, identifier))
                             : generatePublicationReports(model, identifier);
        csvContent.forEach(this::persist);
    }

    private List<ContentWithLocation> generatePublicationReports(Model model, String identifier) {
        return ReportType.getAllTypesExcludingNviReport().stream()
                   .map(reportType -> transform(model, reportType, identifier))
                   .toList();
    }

    private JsonNode readAsJsonNode(UnixPath objectKey) {
        var blob = storageReader.read(objectKey);
        return toJsonNode(blob);
    }

    private void persist(ContentWithLocation contentWithLocation) {
        storageWriter.writeCsv(contentWithLocation.location(), contentWithLocation.content());
        LOGGER.info("Persisted object with key: {}", contentWithLocation.location());
    }

    private void logInput(PersistedResourceEvent input) {
        LOGGER.info("Input object key: {}, eventType: {}", input.key(), input.eventType());
    }
}

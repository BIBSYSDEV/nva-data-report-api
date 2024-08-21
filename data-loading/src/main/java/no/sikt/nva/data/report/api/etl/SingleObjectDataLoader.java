package no.sikt.nva.data.report.api.etl;

import static no.sikt.nva.data.report.api.etl.model.EventType.UPSERT;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import commons.StorageReader;
import commons.StorageWriter;
import commons.ViewCompiler;
import commons.db.utils.DocumentUnwrapper;
import commons.formatter.CsvFormatter;
import commons.model.ContentWithLocation;
import commons.model.DocumentType;
import commons.model.ReportType;
import commons.service.ModelQueryService;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
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

    private static UnixPath constructNewLocation(String folder) {
        return UnixPath.of(folder).addChild(LocalDateTime.now().toString());
    }

    private static ContentWithLocation transform(Model model, ReportType reportType) {
        var result = new ModelQueryService().query(model, reportType);
        var formatted = new CsvFormatter().format(result);
        return new ContentWithLocation(constructNewLocation(reportType.getType()), formatted);
    }

    private void transformAndPersistObject(DocumentType documentType, UnixPath objectKey) {
        var model = loadDataIntoModel(objectKey);
        if (DocumentType.NVI_CANDIDATE.equals(documentType)) {
            var contentWithLocation = transform(model, ReportType.NVI);
            persist(contentWithLocation);
        }
    }

    private void persist(ContentWithLocation contentWithLocation) {
        storageWriter.write(contentWithLocation.location(), contentWithLocation.content());
        LOGGER.info("Persisted object with key: {}", contentWithLocation.location());
    }

    private Model loadDataIntoModel(UnixPath objectKey) {
        var blob = storageReader.read(objectKey);
        var resource = toJsonNode(blob);
        var model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, IoUtils.stringToStream(resource.toString()), Lang.JSONLD);
        return model;
    }

    private void logInput(PersistedResourceEvent input) {
        LOGGER.info("Input object key: {}, eventType: {}", input.key(), input.eventType());
    }
}

package no.sikt.nva.data.report.api.export;

import static commons.utils.GzipUtil.compress;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Optional;
import java.util.UUID;
import no.unit.nva.events.handlers.EventHandler;
import no.unit.nva.events.models.AwsEventBridgeEvent;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class CsvBulkTransformer extends EventHandler<KeyBatchEvent, Void> {

    private final S3Client s3BatchesClient;
    private final S3Client s3OutputClient;
    private String keyBatchesBucket = "keyBatchesBucket";
    private String exportBucket = "exportBucket";

    @JacocoGenerated
    public CsvBulkTransformer() {
        this(defaultS3Client(), defaultS3Client());
    }

    public CsvBulkTransformer(S3Client s3BatchesClient, S3Client s3OutputClient) {
        super(KeyBatchEvent.class);
        this.s3BatchesClient = s3BatchesClient;
        this.s3OutputClient = s3OutputClient;
    }

    @Override
    protected Void processInput(KeyBatchEvent keyBatchEvent, AwsEventBridgeEvent<KeyBatchEvent> event,
                                Context context) {
        var batchResponse = fetchSingleBatch();

        batchResponse.getKey()
            .map(this::extractContent)
            .filter(keys -> !keys.isEmpty())
            .map(this::mapToCsv)
            .map(this::aggregateCsv)
            .map(content -> attempt(() -> compress(content)).orElseThrow())
            .map(this::persist);

        return null;
    }

    @JacocoGenerated
    private static S3Client defaultS3Client() {
        return S3Driver.defaultS3Client().build();
    }

    private boolean persist(byte[] content) {
        var request = PutObjectRequest.builder()
                          .bucket(exportBucket)
                          .key(UUID.randomUUID() + ".gz")
                          .build();
        var response = s3BatchesClient.putObject(request, RequestBody.fromBytes(content));
        return response.sdkHttpResponse().isSuccessful();
    }

    private String aggregateCsv(String csvString) {
        return null;
    }

    private String mapToCsv(String content) {
        return null;
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

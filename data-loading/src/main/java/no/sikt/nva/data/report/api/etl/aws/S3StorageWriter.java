package no.sikt.nva.data.report.api.etl.aws;

import commons.StorageWriter;
import java.nio.charset.StandardCharsets;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3StorageWriter implements StorageWriter {

    private static final String ENCODING = StandardCharsets.UTF_8.name();
    private static final String CONTENT_TYPE = "text/csv; charset=" + ENCODING;
    private static final String FILE_EXTENSION_CSV = ".csv";
    private final S3Client s3Client;
    private final String bucketName;

    @JacocoGenerated
    public S3StorageWriter(String bucketName) {
        this(S3Driver.defaultS3Client().build(), bucketName);
    }

    public S3StorageWriter(S3Client client, String bucket) {
        this.s3Client = client;
        this.bucketName = bucket;
    }

    @Override
    public void writeCsv(UnixPath location, String content) {
        var request = buildRequest(location);
        s3Client.putObject(request, RequestBody.fromString(content));
    }

    private PutObjectRequest buildRequest(UnixPath path) {
        return PutObjectRequest.builder()
                   .bucket(bucketName)
                   .key(path + FILE_EXTENSION_CSV)
                   .contentEncoding(ENCODING)
                   .contentType(CONTENT_TYPE)
                   .build();
    }
}

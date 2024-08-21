package no.sikt.nva.data.report.api.etl.aws;

import static nva.commons.core.attempt.Try.attempt;
import commons.StorageWriter;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;
import software.amazon.awssdk.services.s3.S3Client;

public class S3StorageWriter implements StorageWriter {

    private final S3Driver s3Driver;

    @JacocoGenerated
    public S3StorageWriter(String bucketName) {
        this(S3Driver.defaultS3Client().build(), bucketName);
    }

    public S3StorageWriter(S3Client client, String bucket) {
        this.s3Driver = new S3Driver(client, bucket);
    }

    @Override
    public void write(UnixPath location, String content) {
        attempt(() -> s3Driver.insertFile(location, content)).orElseThrow();
    }
}

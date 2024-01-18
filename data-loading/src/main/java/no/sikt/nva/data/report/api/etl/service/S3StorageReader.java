package no.sikt.nva.data.report.api.etl.service;

import commons.StorageReader;
import no.unit.nva.s3.S3Driver;
import nva.commons.core.paths.UnixPath;
import software.amazon.awssdk.services.s3.S3Client;

public class S3StorageReader implements StorageReader<UnixPath> {

    private final S3Driver s3Driver;

    public S3StorageReader(String bucket) {
        this(S3Driver.defaultS3Client().build(), bucket);
    }

    public S3StorageReader(S3Client client, String bucket) {
        this.s3Driver = new S3Driver(client, bucket);
    }

    @Override
    public String read(UnixPath filename) {
        return s3Driver.getFile(filename);
    }
}

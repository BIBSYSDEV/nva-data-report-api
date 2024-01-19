package no.sikt.nva.data.report.api.etl.model;

import static java.util.Objects.isNull;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UnixPath;

public record PersistedResourceEvent(String bucketName, String key, String eventType) {

    public void validate() {
        validateBucketName();
        validateKey();
        validateEventType();
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return "PersistedResourceEvent{"
               + "bucketName='" + bucketName + '\''
               + ", key='" + key + '\''
               + ", eventType='" + eventType + '\''
               + '}';
    }

    private static void validateParentFolder(PersistedResourceEvent input) {
        UnixPath.of(input.key())
            .getParent()
            .orElseThrow(() -> new IllegalArgumentException("Key must have a parent folder"));
    }

    private void validateEventType() {
        isNullOrBlank(eventType, "Event type cannot be null or blank");
        EventType.parse(eventType);
    }

    private void validateKey() {
        isNullOrBlank(key, "Key cannot be null or blank");
        validateParentFolder(this);
    }

    private void validateBucketName() {
        isNullOrBlank(bucketName, "Bucket name cannot be null or blank");
    }

    private void isNullOrBlank(String key, String message) {
        if (isNull(key) || key.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}

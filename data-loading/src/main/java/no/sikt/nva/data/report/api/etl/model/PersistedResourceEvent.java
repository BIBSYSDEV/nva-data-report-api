package no.sikt.nva.data.report.api.etl.model;

import nva.commons.core.paths.UnixPath;

public record PersistedResourceEvent(String bucketName, String key, String eventType) {

    @Override
    public String toString() {
        return "PersistedResourceEvent{"
               + "bucketName='" + bucketName + '\''
               + ", key='" + key + '\''
               + ", eventType='" + eventType + '\''
               + '}';
    }

    public void validate() {
        validateBucketName();
        validateKey();
        validateEventType();
    }

    private static void validateParentFolder(PersistedResourceEvent input) {
        UnixPath.of(input.key())
            .getParent()
            .orElseThrow(() -> new IllegalArgumentException("Key must have a parent folder"));
    }

    private void validateEventType() {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("Event type cannot be null or blank");
        }
        EventType.parse(eventType);
    }

    private void validateKey() {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be null or blank");
        }
        validateParentFolder(this);
    }

    private void validateBucketName() {
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalArgumentException("Bucket name cannot be null or blank");
        }
    }
}

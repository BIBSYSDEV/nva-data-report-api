package no.sikt.nva.data.report.api.etl.model;

public record PersistedResourceEvent(String bucketName, String key, String eventType) {

    @Override
    public String toString() {
        return "PersistedResourceEvent{"
               + "bucketName='" + bucketName + '\''
               + ", key='" + key + '\''
               + ", eventType='" + eventType + '\''
               + '}';
    }
}

package no.sikt.nva.data.report.api.etl;

public record PersistedResourceMessage(String bucketName, String key) {
    @Override
    public String toString() {
        return "PersistedResourceMessage{"
               + "bucketName='" + bucketName + '\''
               + ", key='" + key + '\''
               + '}';
    }
}

package no.sikt.nva.data.report.api.etl;

public record PersistedResourceEvent(String bucketName, String key, String operation) {

    @Override
    public String toString() {
        return "PersistedResourceEvent{"
               + "bucketName='" + bucketName + '\''
               + ", key='" + key + '\''
               + ", operation='" + operation + '\''
               + '}';
    }
}

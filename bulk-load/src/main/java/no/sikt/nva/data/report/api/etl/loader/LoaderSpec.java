package no.sikt.nva.data.report.api.etl.loader;

import java.net.URI;
import java.util.Locale;

public record LoaderSpec(URI source,
                         Format format,
                         String iamRoleArn,
                         String region,
                         boolean failOnError,
                         Parallelism parallelism,
                         boolean updateSingleCardinalityProperties,
                         boolean queueRequest) {

    @Override
    public String toString() {
        return String.format("""
                                 {"source": "%s",
                                 "format": "%s",
                                 "iamRoleArn": "%s",
                                 "region": "%s",
                                 "failOnError": "%s",
                                 "parallelism": "%s",
                                 "updateSingleCardinalityProperties": "%s",
                                 "queueRequest": "%s",
                                 "dependencies": []
                                 }
                                 """, source, format.toString(), iamRoleArn, region,
                             booleanString(failOnError), parallelism.toString(),
                             booleanString(updateSingleCardinalityProperties),
                             booleanString(queueRequest));
    }

    private String booleanString(boolean value) {
        var string = String.valueOf(value);
        return string.toUpperCase(Locale.ROOT);
    }

    public static final class Builder {

        private URI source;
        private Format format;
        private String iamRoleArn;
        private String region;
        private boolean failOnError;
        private Parallelism parallelism;
        private boolean updateSingleCardinalityProperties;
        private boolean queueRequest;
        public Builder() {
            // Default constructor.
        }

        public Builder withSource(URI source) {
            this.source = source;
            return this;
        }

        public Builder withFormat(Format format) {
            this.format = format;
            return this;
        }

        public Builder withIamRoleArn(String iamRoleArn) {
            this.iamRoleArn = iamRoleArn;
            return this;
        }

        public Builder withRegion(String region) {
            this.region = region;
            return this;
        }

        public Builder withFailOnError(boolean failOnError) {
            this.failOnError = failOnError;
            return this;
        }

        public Builder withParallelism(Parallelism parallelism) {
            this.parallelism = parallelism;
            return this;
        }

        public Builder withUpdateSingleCardinalityProperties(boolean updateSingleCardinalityProperties) {
            this.updateSingleCardinalityProperties = updateSingleCardinalityProperties;
            return this;
        }

        public Builder withQueueRequest(boolean queueRequest) {
            this.queueRequest = queueRequest;
            return this;
        }

        public LoaderSpec build() {
            return new LoaderSpec(source, format, iamRoleArn, region, failOnError, parallelism,
                                  updateSingleCardinalityProperties, queueRequest);
        }
    }
}

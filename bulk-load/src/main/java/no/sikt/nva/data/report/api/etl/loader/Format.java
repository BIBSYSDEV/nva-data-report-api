package no.sikt.nva.data.report.api.etl.loader;

public enum Format {
    NQUADS("nquads");

    private final String string;

    Format(String string) {

        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}

package no.sikt.nva.data.report.api.etl.transformer;

public class MissingIdException extends RuntimeException {

    public MissingIdException() {
        super("Missing id node");
    }
}

package no.sikt.nva.data.report.dbtools.exception;

public class DatabaseResetRequestException extends RuntimeException {

    public DatabaseResetRequestException() {
        super("Resetting database failed");
    }
}

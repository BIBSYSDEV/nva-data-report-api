package no.sikt.nva.data.report.dbtools;

public final class ResetAction {

    public static final String ACTION_INITIATE_DATABASE_RESET = """
        { "action" : "initiateDatabaseReset" }
        """;

    public static final String ACTION_PERFORM_DATABASE_RESET = """
        { "action" : "performDatabaseReset", "token": "%s"}
        """;

    private ResetAction() {
        // NO-OP
    }
}

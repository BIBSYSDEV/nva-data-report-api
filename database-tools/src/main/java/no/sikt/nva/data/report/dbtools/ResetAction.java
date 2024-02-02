package no.sikt.nva.data.report.dbtools;

public final class ResetAction {

    public static final String ACTION_INITIATE_DATABASE_RESET = """
        { "action" : "initiateDatabaseReset" }
        """;

    private ResetAction() {
        // NO-OP
    }
}
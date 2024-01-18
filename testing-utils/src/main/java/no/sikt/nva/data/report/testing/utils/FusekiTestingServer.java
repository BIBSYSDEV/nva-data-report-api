package no.sikt.nva.data.report.testing.utils;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;

public class FusekiTestingServer {

    private FusekiTestingServer() {
        // NO-OP
    }

    public static FusekiServer init(Dataset dataSet, String gspEndpoint) {
        var server = FusekiServer.create()
                         .add(gspEndpoint, dataSet)
                         .build();
        server.start(); // Initialise server before using it!
        return server;
    }
}
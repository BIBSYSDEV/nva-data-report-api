package no.sikt.nva.data.report.api.fetch.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static String getStackTrace(Exception exception) {
        var stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}

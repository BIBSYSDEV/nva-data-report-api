package no.sikt.nva.data.report.api.fetch.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import nva.commons.apigateway.exceptions.BadRequestException;

public final class InstantUtil {

    private static final String TIME_SEPARATOR = "T";

    private static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59);

    private InstantUtil() {
        // NO-OP
    }

    public static Instant before(String date) throws BadRequestException {
        try {
            return isDateTime(date)
                       ? Instant.parse(date)
                       : LocalDate.parse(date).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            throw new BadRequestException("Supply a valid 'before' value in format 'YYYY-MM-DD' or "
                                          + "'YYYY-MM-DDThh:mm:ssZ");
        }
    }

    private static boolean isDateTime(String date) {
        return date.contains(TIME_SEPARATOR);
    }

    public static Instant after(String date) throws BadRequestException {
        try {
            return isDateTime(date)
                       ? Instant.parse(date)
                       : LocalDate.parse(date).atTime(END_OF_DAY).atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            throw new BadRequestException("Supply a valid 'after' value in format 'YYYY-MM-DD' or "
                                          + "'YYYY-MM-DDThh:mm:ssZ");
        }
    }
}

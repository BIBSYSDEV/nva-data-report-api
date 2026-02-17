package no.sikt.nva.data.report.api.fetch.utils;

import static java.util.Objects.nonNull;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.query.ResultSet;

public final class ResultUtil {

    private static final String PUBLICATION_IDENTIFIER_VAR = "publicationIdentifier";

    private ResultUtil() {
    }

    public static boolean isNotEmpty(ResultSet result) {
        return result.getRowNumber() > 0;
    }

    public static DataResult extractData(ResultSet resultSet) {
        var headers = resultSet.getResultVars();
        var data = new ArrayList<List<String>>();
        String cursor = null;
        while (resultSet.hasNext()) {
            var row = resultSet.next();
            var rowData = new ArrayList<String>();
            for (String header : headers) {
                var cell = row.get(header);
                rowData.add(nonNull(cell) ? cell.toString() : EMPTY_STRING);
            }
            data.add(rowData);
            if (!resultSet.hasNext()) {
                cursor = row.get(PUBLICATION_IDENTIFIER_VAR).toString();
            }
        }
        return new DataResult(cursor, data);
    }

    public static List<String> extractHeaders(ResultSet resultSet) {
        return resultSet.getResultVars();
    }

    public record DataResult (String cursor, List<List<String>> data) {
    }
}

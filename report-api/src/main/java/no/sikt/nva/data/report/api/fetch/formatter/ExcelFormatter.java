package no.sikt.nva.data.report.api.fetch.formatter;

import static java.util.Objects.nonNull;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import commons.formatter.ResponseFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;
import org.apache.jena.query.ResultSet;

public class ExcelFormatter implements ResponseFormatter {

    private static final Encoder ENCODER = Base64.getEncoder();

    @Override
    public String format(ResultSet resultSet) {
        var headers = extractHeaders(resultSet);
        var data = extractData(headers, resultSet);
        var excel = Excel.fromJava(headers, data);
        return ENCODER.encodeToString(excel.toBytes());
    }

    private List<List<String>> extractData(List<String> headers, ResultSet resultSet) {
        var data = new ArrayList<List<String>>();
        while (resultSet.hasNext()) {
            var row = resultSet.next();
            var rowData = new ArrayList<String>();
            for (String header : headers) {
                var cell = row.get(header);
                rowData.add(nonNull(cell) ? cell.toString() : EMPTY_STRING);
            }
            data.add(rowData);
        }
        return data;
    }

    private List<String> extractHeaders(ResultSet resultSet) {
        return resultSet.getResultVars();
    }
}

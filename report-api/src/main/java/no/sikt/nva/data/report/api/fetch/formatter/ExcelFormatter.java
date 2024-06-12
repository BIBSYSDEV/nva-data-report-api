package no.sikt.nva.data.report.api.fetch.formatter;

import static no.sikt.nva.data.report.api.fetch.utils.ResultUtil.extractData;
import static no.sikt.nva.data.report.api.fetch.utils.ResultUtil.extractHeaders;
import commons.formatter.ResponseFormatter;
import java.util.Base64;
import java.util.Base64.Encoder;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;
import org.apache.jena.query.ResultSet;

public class ExcelFormatter implements ResponseFormatter {

    private static final Encoder ENCODER = Base64.getEncoder();

    @Override
    public String format(ResultSet resultSet) {
        var headers = extractHeaders(resultSet);
        var data = extractData(resultSet);
        var excel = Excel.fromJava(headers, data);
        return ENCODER.encodeToString(excel.toBytes());
    }
}

package no.sikt.nva.data.report.api.fetch.formatter;

import commons.formatter.ResponseFormatter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;
import org.apache.jena.query.ResultSet;

public class ExcelFormatter implements ResponseFormatter {

    private static final Encoder ENCODER = Base64.getEncoder();

    @Override
    public String format(ResultSet resultSet) {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        //TODO: Actually use the result set to create the excel
        var excel = Excel.fromJava(List.of("Some header"), List.of(List.of("Some data")));
        try {
            excel.write(byteArrayOutputStream);
        } catch (IOException e) {
            //TODO: Handle exception
            //TODO: Test this
            throw new RuntimeException(e);
        }
        var bytes = byteArrayOutputStream.toByteArray();
        return ENCODER.encodeToString(bytes);
    }
}

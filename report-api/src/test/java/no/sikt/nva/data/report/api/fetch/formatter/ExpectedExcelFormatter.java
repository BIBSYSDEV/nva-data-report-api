package no.sikt.nva.data.report.api.fetch.formatter;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;

public class ExpectedExcelFormatter {

    public static Excel generateExcel(String csvString) {
        try (CSVReader csvReader = new CSVReader(new StringReader(csvString))) {
            var headers = Arrays.stream(csvReader.readNext()).toList();
            var rows = csvReader.readAll().stream()
                           .map(array -> Arrays.stream(array).toList())
                           .toList();

            return Excel.fromJava(headers, rows);
        } catch (CsvException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

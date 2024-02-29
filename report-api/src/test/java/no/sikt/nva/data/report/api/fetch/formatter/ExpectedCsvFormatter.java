package no.sikt.nva.data.report.api.fetch.formatter;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExpectedCsvFormatter {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String QUOTED_STRING_TEMPLATE = "\"%s\"";
    public static final int QUOTE_LENGTH = 2;
    public static final String VALUE_ROW_SEPARATOR = "-";
    public static final String HEADER_ROW_SEPARATOR = "=";
    public static final String COLUMN_SEPARATOR = "|";

    public static String generateTable(String csvString) {
        try (CSVReader csvReader = new CSVReader(new StringReader(csvString))) {
            var headers = csvReader.readNext();
            var rows = csvReader.readAll();

            var maxLengths = calculateMaxLengths(headers, rows);

            return printTable(headers, rows, maxLengths);
        } catch (CsvException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int[] calculateMaxLengths(String[] headers, List<String[]> rows) {
        return IntStream.range(0, headers.length)
                   .map(i -> getMaxCellLength(i, headers, rows))
                   .toArray();
    }

    private static int getMaxCellLength(int index, String[] headers, List<String[]> rows) {
        return Math.max(
            Arrays.stream(rows.toArray(new String[0][0]))
                .mapToInt(row -> row[index].length())
                .map(ExpectedCsvFormatter::addQuoteLength)
                .max()
                .orElse(0),
            headers[index].length());
    }

    private static int addQuoteLength(int value) {
        return value + QUOTE_LENGTH;
    }

    private static String printTable(String[] headers, List<String[]> rows, int[] maxLengths) {
        var tableWidth = calculateTableWidth(maxLengths);
        var horizontalLine = VALUE_ROW_SEPARATOR.repeat(tableWidth);

        return horizontalLine + LINE_SEPARATOR
               + printRow(headers, maxLengths, true) + LINE_SEPARATOR
               + HEADER_ROW_SEPARATOR.repeat(tableWidth) + LINE_SEPARATOR
               + rows.stream()
                     .map(row -> printRow(row, maxLengths, false))
                     .collect(Collectors.joining(LINE_SEPARATOR))
               + LINE_SEPARATOR
               + horizontalLine + LINE_SEPARATOR;
    }

    private static int calculateTableWidth(int[] maxLengths) {
        return Arrays.stream(maxLengths).sum() + 1 + (3 * maxLengths.length);
    }

    private static String printRow(String[] row, int[] maxLengths, boolean isHeader) {
        return IntStream.range(0, row.length)
                   .mapToObj(i -> formatCell(i, row, maxLengths, isHeader))
                   .collect(Collectors.joining()) + COLUMN_SEPARATOR;
    }

    private static String formatCell(int index, String[] row, int[] maxLengths, boolean isHeader) {
        var cell = row[index];
        var value = isHeader || cell.isBlank() ? cell : String.format(QUOTED_STRING_TEMPLATE, cell);
        return String.format("| %-" + maxLengths[index] + "s ", value);
    }
}

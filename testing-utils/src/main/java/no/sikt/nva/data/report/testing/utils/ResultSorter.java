package no.sikt.nva.data.report.testing.utils;

import static no.sikt.nva.data.report.testing.utils.StringUtils.buildString;
import static no.sikt.nva.data.report.testing.utils.StringUtils.printAsString;
import static no.sikt.nva.data.report.testing.utils.StringUtils.scanData;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import no.sikt.nva.data.report.testing.utils.StringUtils.ScanningResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ResultSorter {

    public static final String TARGET = "\"";
    public static final String CSV = "CSV";
    private static final int RESULT_HEADER_LAST_INDEX = 1;
    private static final int RESULT_ENDING_FORMATTED_LINE = 1;
    private static final String COLUMN_SPLIT_REGEX = "\\|";

    private ResultSorter() {
        // NO-OP
    }

    public static String sortResponse(String type, String data, String sortByHeader1, String sortByHeader2)
        throws IOException {
        var headers = getHeaders(data);
        var primaryIndex = headers.indexOf(sortByHeader1);
        var secondaryIndex = headers.indexOf(sortByHeader2);
        return CSV.equals(type)
                   ? sortCsv(data, sortByHeader1, sortByHeader2)
                   : sortTextPlain(data, primaryIndex, secondaryIndex);
    }

    public static List<String> extractDataLines(String data) {
        var scanningResult = scanData(data);
        return scanningResult.lines()
                   .subList(RESULT_HEADER_LAST_INDEX,
                            scanningResult.lines().size() - RESULT_ENDING_FORMATTED_LINE);
    }

    public static List<String> sortDataLines(ScanningResult scanningResult, int primaryIndex, int secondaryIndex) {
        var dataLines = scanningResult.lines().subList(0, scanningResult.lines().size() - 1);
        dataLines.sort(Comparator.comparing((String line) -> {
            var columns = line.split(COLUMN_SPLIT_REGEX);
            return columns.length > primaryIndex ? columns[primaryIndex].trim() : EMPTY_STRING;
        }).thenComparing(line -> {
            var columns = line.split(COLUMN_SPLIT_REGEX);
            return columns.length > secondaryIndex ? columns[secondaryIndex].trim() : EMPTY_STRING;
        }));
        return dataLines;
    }

    private static List<String> getHeaders(String data) {
        return Arrays.stream(data.split(System.lineSeparator())[1].split(COLUMN_SPLIT_REGEX))
                   .map(String::strip)
                   .map(string -> string.replace(TARGET, EMPTY_STRING))
                   .toList();
    }

    private static String sortCsv(String data, String sortByHeader1, String sortByHeader2) throws IOException {
        var stringReader = new StringReader(data);
        var format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
        var csvParser = format.parse(stringReader);
        var sortedCsvRecords = sortCsvRecords(csvParser, sortByHeader1, sortByHeader2);
        return printAsString(format, csvParser, sortedCsvRecords);
    }

    private static String sortTextPlain(String data, int primaryIndex, int secondaryIndex) {
        var scanningResult = scanData(data);
        var dataLines = sortDataLines(scanningResult, primaryIndex, secondaryIndex);
        return buildString(scanningResult, dataLines);
    }

    private static ArrayList<CSVRecord> sortCsvRecords(CSVParser csvParser, String sortByHeader1,
                                                       String sortByHeader2) {
        var csvRecords = new ArrayList<CSVRecord>();
        csvParser.forEach(csvRecords::add);
        csvRecords.sort(Comparator.comparing((CSVRecord record) -> record.get(sortByHeader1))
                            .thenComparing((CSVRecord record) -> record.get(sortByHeader2)));
        return csvRecords;
    }
}

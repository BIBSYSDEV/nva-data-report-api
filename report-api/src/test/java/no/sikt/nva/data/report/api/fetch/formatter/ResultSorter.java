package no.sikt.nva.data.report.api.fetch.formatter;

import static no.sikt.nva.data.report.api.fetch.formatter.StringUtils.buildString;
import static no.sikt.nva.data.report.api.fetch.formatter.StringUtils.printAsString;
import static no.sikt.nva.data.report.api.fetch.formatter.StringUtils.scanData;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.formatter.StringUtils.ScanningResult;
import no.sikt.nva.data.report.api.fetch.model.ReportFormat;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ResultSorter {

    private static final int RESULT_HEADER_LAST_INDEX = 1;
    private static final int RESULT_ENDING_FORMATTED_LINE = 1;
    private static final String COLUMN_SPLIT_REGEX = "\\|";

    private ResultSorter() {
        // NO-OP
    }

    public static String sortResponse(ReportFormat type, String data, String sortByHeader1,
                                      String contributorIdentifier) throws IOException {
        return ReportFormat.CSV.equals(type)
                   ? sortCsv(data, sortByHeader1, contributorIdentifier)
                   : sortTextPlain(data);
    }

    public static List<String> extractDataLines(String data) {
        var scanningResult = scanData(data);
        return scanningResult.lines()
                   .subList(RESULT_HEADER_LAST_INDEX,
                            scanningResult.lines().size() - RESULT_ENDING_FORMATTED_LINE);
    }

    private static String sortCsv(String data, String sortByHeader1, String contributorIdentifier) throws IOException {
        var stringReader = new StringReader(data);
        var format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
        var csvParser = format.parse(stringReader);
        var sortedCsvRecords = sortCsvRecords(csvParser, sortByHeader1, contributorIdentifier);
        return printAsString(format, csvParser, sortedCsvRecords);
    }

    private static String sortTextPlain(String data) {
        var scanningResult = scanData(data);
        var dataLines = sortDataLines(scanningResult);
        return buildString(scanningResult, dataLines);
    }

    private static ArrayList<CSVRecord> sortCsvRecords(CSVParser csvParser, String sortByHeader1, String sortByHeader2) {
        var csvRecords = new ArrayList<CSVRecord>();
        csvParser.forEach(csvRecords::add);
        csvRecords.sort(Comparator.comparing((CSVRecord record) -> record.get(sortByHeader1))
                            .thenComparing((CSVRecord record) -> record.get(sortByHeader2)));
        return csvRecords;
    }

    private static List<String> sortDataLines(ScanningResult scanningResult) {
        var dataLines = scanningResult.lines().subList(0, scanningResult.lines().size() - 1);
        dataLines.sort(Comparator.comparing((String line) -> {
            var columns = line.split(COLUMN_SPLIT_REGEX);
            return columns.length > 1 ? columns[1].trim() : EMPTY_STRING;
        }).thenComparing(line -> {
            var columns = line.split(COLUMN_SPLIT_REGEX);
            return columns.length > 2 ? columns[2].trim() : EMPTY_STRING;
        }));
        return dataLines;
    }
}

package no.sikt.nva.data.report.api.fetch.formatter;

import static no.sikt.nva.data.report.api.fetch.testutils.generator.TestData.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.TestData.PUBLICATION_ID;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import no.sikt.nva.data.report.api.fetch.CustomMediaType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class ResultSorter {

    public static final String COLUMN_SPLIT_REGEX = "\\|";
    private static final String LINE_BREAK = "\n";
    private static final String EMPTY_STRING = "";

    private ResultSorter() {
        // NO-OP
    }

    public static String sortResponse(MediaType type, String data) throws IOException {
        return CustomMediaType.TEXT_CSV.equals(type) ? sortCsv(data) : sortTextPlain(data);
    }

    private static String sortCsv(String data) throws IOException {
        var stringReader = new StringReader(data);
        var format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
        var csvParser = format.parse(stringReader);
        var sortedCsvRecords = sortCsvRecords(csvParser);
        return printAsString(format, csvParser, sortedCsvRecords);
    }

    private static String sortTextPlain(String data) {
        var scanningResult = scanData(data);
        var dataLines = sortDataLines(scanningResult);
        return buildString(scanningResult, dataLines);
    }

    private static ArrayList<CSVRecord> sortCsvRecords(CSVParser csvParser) {
        var csvRecords = new ArrayList<CSVRecord>();
        csvParser.forEach(csvRecords::add);
        csvRecords.sort(Comparator.comparing((CSVRecord record) -> record.get(PUBLICATION_ID))
                            .thenComparing((CSVRecord record) -> record.get(CONTRIBUTOR_IDENTIFIER)));
        return csvRecords;
    }

    private static String printAsString(CSVFormat format, CSVParser csvParser, ArrayList<CSVRecord> recordList)
        throws IOException {
        var stringBuilder = new StringBuilder();
        try (var printer = new CSVPrinter(stringBuilder, format)) {
            printer.printRecord(csvParser.getHeaderMap().keySet());
            for (CSVRecord record : recordList) {
                printer.printRecord(record);
            }
        }
        return stringBuilder.toString();
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

    private static String buildString(ScanningResult scanningResult, List<String> dataLines) {
        var sortedData = new StringBuilder(scanningResult.header())
                             .append(LINE_BREAK)
                             .append(scanningResult.separator());

        for (String line : dataLines) {
            sortedData.append(LINE_BREAK).append(line);
        }
        sortedData.append(LINE_BREAK).append(scanningResult.lines().getLast());
        sortedData.append(LINE_BREAK);
        return sortedData.toString();
    }

    private static ScanningResult scanData(String data) {
        var scanner = new Scanner(data);
        var header = scanner.nextLine();
        var separator = scanner.nextLine();
        var lines = new ArrayList<String>();

        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        return new ScanningResult(header, separator, lines);
    }

    private record ScanningResult(String header, String separator, ArrayList<String> lines) {

    }
}

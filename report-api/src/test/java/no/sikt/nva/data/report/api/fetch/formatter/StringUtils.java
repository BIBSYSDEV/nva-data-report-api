package no.sikt.nva.data.report.api.fetch.formatter;

import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public final class StringUtils {

    private static final String LINE_BREAK = "\n";
    private static final String DELIMITER = ",";

    private StringUtils() {
        // NO-OP
    }

    public static String buildString(ScanningResult scanningResult, List<String> dataLines) {
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

    public static ScanningResult scanData(String data) {
        var scanner = new Scanner(data);
        var header = scanner.nextLine();
        var separator = scanner.nextLine();
        var lines = new ArrayList<String>();

        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        return new ScanningResult(header, separator, lines);
    }

    public static String printAsString(CSVFormat format, CSVParser csvParser, ArrayList<CSVRecord> recordList)
        throws IOException {
        var stringBuilder = new StringBuilder();
        try (var printer = new CSVPrinter(stringBuilder, format)) {
            printer.printRecord(csvParser.getHeaderMap().keySet());
            for (CSVRecord record : recordList) {
                if (Arrays.stream(record.values()).allMatch(EMPTY_STRING::equals)) {
                    addNumberOfDelimiters(stringBuilder, record.size() - 1);
                    break;
                }
                printer.printRecord(record);
            }
        }
        return stringBuilder.toString();
    }

    public static StringBuilder addNumberOfDelimiters(StringBuilder stringBuilder, int numberOfDelimiters) {
        return stringBuilder.append(DELIMITER.repeat(Math.max(0, numberOfDelimiters)));
    }

    public record ScanningResult(String header, String separator, ArrayList<String> lines) {

    }
}

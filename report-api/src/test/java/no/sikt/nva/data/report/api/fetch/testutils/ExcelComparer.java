package no.sikt.nva.data.report.api.fetch.testutils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

public final class ExcelComparer {

    public static void assertExcelEquals(Excel expected, Excel actual) {
        try (var expectedWorkbook = expected.workbook(); var actualWorkbook = actual.workbook()) {
            assertEquals(expectedWorkbook.getNumberOfSheets(), actualWorkbook.getNumberOfSheets());
            IntStream.range(0, expectedWorkbook.getNumberOfSheets()).forEach(i -> {
                var expectedSheet = expectedWorkbook.getSheetAt(i);
                var actualSheet = actualWorkbook.getSheetAt(i);
                assertEquals(expectedSheet.getPhysicalNumberOfRows(), actualSheet.getPhysicalNumberOfRows());

                assertEqualHeaders(expectedSheet, actualSheet);

                var expectedData = getData(expectedSheet, expectedSheet.getPhysicalNumberOfRows());
                var actualData = getData(actualSheet, actualSheet.getPhysicalNumberOfRows());
                assertEquals(new HashSet<>(expectedData), new HashSet<>(actualData));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertEqualHeaders(Sheet expectedSheet, Sheet actualSheet) {
        var expectedHeader = expectedSheet.getRow(0);
        var actualHeader = actualSheet.getRow(0);
        IntStream.range(0, expectedHeader.getPhysicalNumberOfCells()).forEach(k -> {
            var expectedCell = expectedHeader.getCell(k);
            var actualCell = actualHeader.getCell(k);
            assertEquals(expectedCell.getStringCellValue(), actualCell.getStringCellValue());
        });
    }

    private static List<List<String>> getData(Sheet sheet, int endRow) {
        return IntStream.range(1, endRow)
                   .mapToObj(sheet::getRow)
                   .map(row -> IntStream.range(0, row.getPhysicalNumberOfCells())
                                   .mapToObj(row::getCell)
                                   .map(Cell::getStringCellValue)
                                   .collect(Collectors.toList()))
                   .collect(Collectors.toList());
    }
}

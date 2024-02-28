package no.sikt.nva.data.report.api.fetch.xlsx;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Excel {

    private final Workbook workbook;

    private Excel(Workbook workbook) {
        this.workbook = workbook;
    }

    public static Excel fromJava(List<String> headers, List<List<String>> data) {
        var excel = new Excel(new XSSFWorkbook());
        var sheet = excel.workbook.createSheet();
        addHeadersToSheet(headers, sheet);
        addDataToSheet(data, sheet);
        return excel;
    }

    public void write(OutputStream outputStream) throws IOException {
        workbook.write(outputStream);
        workbook.close();
    }

    private static void addHeadersToSheet(List<String> headers, Sheet sheet) {
        var header = sheet.createRow(0);
        var headerStyle = getCellStyle(sheet);

        for (var headerCounter = 0; headerCounter < headers.size(); headerCounter++) {
            var headerCell = header.createCell(headerCounter);
            headerCell.setCellStyle(headerStyle);
            headerCell.setCellValue(headers.get(headerCounter));
        }
    }

    private static CellStyle getCellStyle(Sheet sheet) {
        var headerStyle = sheet.getWorkbook().createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }

    private static void addDataToSheet(List<List<String>> data, Sheet sheet) {
        for (var counter = 0; counter < data.size(); counter++) {
            var currentRow = sheet.createRow(counter + 1);
            var rowData = data.get(counter);
            for (var subCounter = 0; subCounter < rowData.size(); subCounter++) {
                var currentCell = currentRow.createCell(subCounter);
                currentCell.setCellValue(rowData.get(subCounter));
            }
        }
    }
}

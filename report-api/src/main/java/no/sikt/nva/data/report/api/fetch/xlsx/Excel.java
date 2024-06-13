package no.sikt.nva.data.report.api.fetch.xlsx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public record Excel(Workbook workbook) {

    public static Excel fromJava(List<String> headers, List<List<String>> data) {
        var excel = new Excel(createWorkbookWithOneSheet());
        excel.addHeaders(headers);
        excel.addData(data);
        return excel;
    }

    public static Excel errorReport() {
        var excel = new Excel(createWorkbookWithOneSheet());
        excel.addData(List.of(List.of("Unexpected error occurred. Please contact support.")));
        return excel;
    }

    public void addData(List<List<String>> data) {
        var sheet = workbook.getSheetAt(0);
        for (List<String> cells : data) {
            var nextRow = sheet.getLastRowNum() + 1;
            addCells(sheet.createRow(nextRow), cells);
        }
    }

    public byte[] toBytes() {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            this.write(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Excel postProcess(List<PostProcessFunction> processFunctions) {
        processFunctions.forEach(value -> {
            var sheet = workbook.getSheetAt(0);
            var headerRow = sheet.getRow(0);
            var headerIndex = findHeaderIndex(headerRow, value.getHeader());
            for (Row currentRow : sheet) {
                var currentCell = currentRow.getCell(headerIndex);
                currentCell.setCellValue(value.getPostProcessor().apply(currentCell.getStringCellValue()));
            }
        });
        return this;
    }

    private static void addCells(Row row, List<String> cells) {
        for (var subCounter = 0; subCounter < cells.size(); subCounter++) {
            var currentCell = row.createCell(subCounter);
            currentCell.setCellValue(cells.get(subCounter));
        }
    }

    private static XSSFWorkbook createWorkbookWithOneSheet() {
        var workbook = new XSSFWorkbook();
        workbook.createSheet();
        return workbook;
    }

    private int findHeaderIndex(Row headerRow, String header) {
        for (var counter = 0; counter < headerRow.getLastCellNum(); counter++) {
            if (headerRow.getCell(counter).getStringCellValue().equals(header)) {
                return counter;
            }
        }
        throw new IllegalArgumentException("Header not found: " + header);
    }

    private void write(OutputStream outputStream) throws IOException {
        workbook.write(outputStream);
        workbook.close();
    }

    private void addHeaders(List<String> headers) {
        var sheet = workbook.getSheetAt(0);
        var headerRow = sheet.createRow(0);
        addCells(headerRow, headers);
    }
}

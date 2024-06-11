package no.sikt.nva.data.report.api.fetch.xlsx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Excel {

    private final Workbook workbook;
    private final Sheet sheet;

    public Excel(Workbook workbook) {
        this.workbook = workbook;
        this.sheet = workbook.getSheetAt(0);
    }

    public static Excel fromJava(List<String> headers, List<List<String>> data) {
        var workbook = createWorkbookWithOneSheet();
        var excel = new Excel(workbook);
        excel.addHeaders(headers);
        excel.addData(data);
        return excel;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void addData(List<List<String>> data) {
        for (var counter = sheet.getLastRowNum(); counter < data.size(); counter++) {
            var currentRow = sheet.createRow(counter + 1);
            var rowData = data.get(counter);
            for (var subCounter = 0; subCounter < rowData.size(); subCounter++) {
                var currentCell = currentRow.createCell(subCounter);
                currentCell.setCellValue(rowData.get(subCounter));
            }
        }
    }

    public byte[] toBytes() {
        var byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            this.write(byteArrayOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static XSSFWorkbook createWorkbookWithOneSheet() {
        var workbook = new XSSFWorkbook();
        workbook.createSheet();
        return workbook;
    }

    private void write(OutputStream outputStream) throws IOException {
        workbook.write(outputStream);
        workbook.close();
    }

    private void addHeaders(List<String> headers) {
        var header = sheet.createRow(0);
        for (var headerCounter = 0; headerCounter < headers.size(); headerCounter++) {
            var headerCell = header.createCell(headerCounter);
            headerCell.setCellValue(headers.get(headerCounter));
        }
    }
}

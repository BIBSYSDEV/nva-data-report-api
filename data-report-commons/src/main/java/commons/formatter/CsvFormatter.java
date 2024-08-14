package commons.formatter;

import static java.util.Objects.isNull;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import nva.commons.core.JacocoGenerated;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

@JacocoGenerated //Tested in modules report-api and bulk-export
public final class CsvFormatter implements ResponseFormatter {

    private static final String DECIMAL_PATTERN = "0.0000";
    private static final String TYPE_INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
    private static final String TYPE_DOUBLE = "http://www.w3.org/2001/XMLSchema#double";

    @Override
    public String format(ResultSet resultSet) {
        var headers = resultSet.getResultVars();
        var stringWriter = new StringWriter();
        try (CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.DEFAULT)) {
            printHeaders(csvPrinter, headers);
            printRows(csvPrinter, resultSet, headers);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
        return stringWriter.toString();
    }

    private static void printRows(CSVPrinter csvPrinter, ResultSet resultSet, List<String> headers)
        throws IOException {
        while (resultSet.hasNext()) {
            printRow(csvPrinter, headers, resultSet.next());
        }
    }

    private static void printRow(CSVPrinter csvPrinter, List<String> headers, QuerySolution querySolution)
        throws IOException {
        var rowData = new ArrayList<>();
        headers.forEach(header -> addHeaderValueInRow(rowData, querySolution.get(header)));
        csvPrinter.printRecord(rowData);
    }

    private static void addHeaderValueInRow(ArrayList<Object> rowData, RDFNode rdfNode) {
        if (isNull(rdfNode)) {
            rowData.add(EMPTY_STRING);
        } else if (isDoubleLiteral(rdfNode)) {
            var formattedDouble = formatDouble(rdfNode.asLiteral());
            rowData.add(formattedDouble);
        } else if (isIntegerLiteral(rdfNode)) {
            rowData.add(rdfNode.asLiteral().getInt());
        } else {
            rowData.add(rdfNode.toString());
        }
    }

    private static void printHeaders(CSVPrinter csvPrinter, List<String> resultVars) throws IOException {
        csvPrinter.printRecord(resultVars);
    }

    private static String formatDouble(Literal literal) {
        return new DecimalFormat(DECIMAL_PATTERN, DecimalFormatSymbols.getInstance()).format(literal.getDouble());
    }

    private static boolean isIntegerLiteral(RDFNode rdfNode) {
        return isLiteral(rdfNode) && isInteger(rdfNode);
    }

    private static boolean isInteger(RDFNode rdfNode) {
        return TYPE_INTEGER.equals(((Literal) rdfNode).getDatatypeURI());
    }

    private static boolean isLiteral(RDFNode rdfNode) {
        return rdfNode instanceof Literal;
    }

    private static boolean isDoubleLiteral(RDFNode rdfNode) {
        return isLiteral(rdfNode) && isDouble(rdfNode);
    }

    private static boolean isDouble(RDFNode rdfNode) {
        return TYPE_DOUBLE.equals(((Literal) rdfNode).getDatatypeURI());
    }
}

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
        var resultVars = resultSet.getResultVars();
        var writer = new StringWriter();
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printHeaders(csvPrinter, resultVars);
            printRows(resultSet, resultVars, csvPrinter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    private static void printRows(ResultSet resultSet, List<String> resultVars, CSVPrinter csvPrinter)
        throws IOException {
        while (resultSet.hasNext()) {
            printRow(resultVars, csvPrinter, resultSet.next());
        }
    }

    private static void printRow(List<String> headers, CSVPrinter csvPrinter, QuerySolution querySolution)
        throws IOException {
        var rowData = new ArrayList<>();
        headers.forEach(header -> buildRow(rowData, querySolution.get(header)));
        csvPrinter.printRecord(rowData);
    }

    private static void buildRow(ArrayList<Object> rowData, RDFNode rdfNode) {
        addRdfNode(rdfNode, rowData);
    }

    private static void addRdfNode(RDFNode rdfNode, ArrayList<Object> rowData) {
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

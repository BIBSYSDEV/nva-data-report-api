package commons.formatter;

import static org.apache.commons.io.StandardLineSeparator.CRLF;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import nva.commons.core.JacocoGenerated;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

@JacocoGenerated //Tested in modules report-api and bulk-export
public final class CsvFormatter implements ResponseFormatter {

    public static final String DOUBLE_QUOTES = "\"";
    private static final String DECIMAL_PATTERN = "0.0000";
    private static final String DELIMITER = ",";
    private static final String TYPE_INTEGER = "http://www.w3.org/2001/XMLSchema#integer";
    private static final String TYPE_DOUBLE = "http://www.w3.org/2001/XMLSchema#double";
    private static final String LINE_BREAK = CRLF.getString();

    @Override
    public String format(ResultSet resultSet) {
        var resultVars = resultSet.getResultVars();

        var csvBuilder = new StringBuilder();
        appendHeaders(resultVars, csvBuilder);

        while (resultSet.hasNext()) {
            appendResults(resultSet, resultVars, csvBuilder);
        }

        return csvBuilder.toString();
    }

    private static void appendHeaders(List<String> resultVars, StringBuilder csvBuilder) {
        for (String var : resultVars) {
            appendDouble(csvBuilder, var);
        }
        removeLastComma(csvBuilder);
        appendLineBreak(csvBuilder);
    }

    private static void appendResults(ResultSet resultSet, List<String> resultVars, StringBuilder csvBuilder) {
        var row = resultSet.next();
        appendRow(resultVars, csvBuilder, row);
        removeLastComma(csvBuilder);
        appendLineBreak(csvBuilder);
    }

    private static void appendRow(List<String> resultVars, StringBuilder csvBuilder, QuerySolution row) {
        for (String var : resultVars) {
            var rdfNode = row.get(var);
            if (rdfNode == null) {
                appendEmptyString(csvBuilder);
            } else if (isDoubleLiteral(rdfNode)) {
                var formattedDouble = formatDouble(rdfNode.asLiteral());
                appendDouble(csvBuilder, formattedDouble);
            } else if (isIntegerLiteral(rdfNode)) {
                appendInt(csvBuilder, rdfNode.asLiteral().getInt());
            } else {
                appendRdfNode(csvBuilder, rdfNode);
            }
        }
    }

    private static void appendEmptyString(StringBuilder csvBuilder) {
        csvBuilder.append(DELIMITER);
    }

    private static void appendRdfNode(StringBuilder csvBuilder, RDFNode node) {
        var nodeValue = node.toString();
        if (nodeValue.contains(DELIMITER)) {
            nodeValue = DOUBLE_QUOTES + nodeValue + DOUBLE_QUOTES;
        }
        csvBuilder.append(nodeValue).append(DELIMITER);
    }

    private static void appendDouble(StringBuilder csvBuilder, String value) {
        csvBuilder.append(value).append(DELIMITER);
    }

    private static void appendInt(StringBuilder csvBuilder, int value) {
        csvBuilder.append(value).append(DELIMITER);
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

    private static void appendLineBreak(StringBuilder csvBuilder) {
        csvBuilder.append(LINE_BREAK);
    }

    private static void removeLastComma(StringBuilder csvBuilder) {
        csvBuilder.deleteCharAt(csvBuilder.length() - 1);
    }
}

package no.sikt.nva.data.report.api.fetch;

import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_CSV;
import static no.sikt.nva.data.report.api.fetch.CustomMediaType.TEXT_PLAIN;
import static no.sikt.nva.data.report.api.fetch.formatter.ExpectedCsvFormatter.generateTable;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.MediaType;
import commons.db.DatabaseConnection;
import commons.db.GraphStoreProtocolConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import no.sikt.nva.data.report.api.fetch.model.InstantUtil;
import no.sikt.nva.data.report.api.fetch.model.ReportType;
import no.sikt.nva.data.report.api.fetch.service.QueryService;
import no.sikt.nva.data.report.api.fetch.testutils.BadRequestProvider;
import no.sikt.nva.data.report.api.fetch.testutils.TestingRequest;
import no.sikt.nva.data.report.api.fetch.testutils.ValidRequestSource;
import no.sikt.nva.data.report.api.fetch.testutils.generator.TestData;
import no.sikt.nva.data.report.api.fetch.testutils.generator.TestData.DatePair;
import no.sikt.nva.data.report.api.fetch.testutils.generator.publication.PublicationDate;
import no.sikt.nva.data.report.testing.utils.FusekiTestingServer;
import no.sikt.nva.data.report.testing.utils.RawFormatter;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

class FetchDataReportTest {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String TEMPLATE_DIRECTORY = "template";
    public static final String SPARQL = ".sparql";
    public static final String BEFORE_PLACEHOLDER = "__BEFORE__";
    public static final String AFTER_PLACEHOLDER = "__AFTER__";
    public static final String PUBLICATION_URI_SPARQL = "publicationUri";
    private static final String GSP_ENDPOINT = "/gsp";
    private static final URI GRAPH = URI.create("https://example.org/graph");
    private static final int DEFAULT_OFFSET = 0;
    private static FusekiServer server;
    private static DatabaseConnection databaseConnection;

    @BeforeAll
    static void setup() {
        var dataSet = DatasetFactory.createTxnMem();
        server = FusekiTestingServer.init(dataSet, GSP_ENDPOINT);
        var url = server.serverURL();
        var queryPath = new Environment().readEnv("QUERY_PATH");
        databaseConnection = new GraphStoreProtocolConnection(url, queryPath);
    }

    @AfterAll
    static void tearDown() {
        server.stop();
    }

    @AfterEach
    void clearDatabase() {
        try {
            databaseConnection.delete(GRAPH);
        } catch (Exception e) {
            // Necessary to avoid case where we hve already deleted the graph
            catchExpectedExceptionsExceptHttpException(e);
        }
    }

    @ParameterizedTest()
    @DisplayName("Should throw BadRequestException when input is invalid")
    @ArgumentsSource(BadRequestProvider.class)
    void shouldThrowBadRequest(TestingRequest report)
        throws IOException {
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var output = new ByteArrayOutputStream();
        handler.handleRequest(generateHandlerRequest(report), output, new FakeContext());
        var response = GatewayResponse.fromOutputStream(output, String.class);
        assertEquals(400, response.getStatusCode());
    }

    @ParameterizedTest
    @ArgumentsSource(ValidRequestSource.class)
    void shouldReturnFormattedResult(TestingRequest request) throws IOException, BadRequestException {
        var testData = new TestData(List.of(new DatePair(new PublicationDate("2023", "02", "02"),
                                                         Instant.now().minus(100, ChronoUnit.DAYS)),
                                            new DatePair(new PublicationDate("2023", "10", "18"),
                                                         Instant.now().minus(100, ChronoUnit.DAYS))));
        databaseConnection.write(GRAPH, toTriples(testData.getModel()), Lang.NTRIPLES);
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var input = generateHandlerRequest(request);
        var output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, new FakeContext());
        var response = GatewayResponse.fromOutputStream(output, String.class);
        assertEquals(200, response.getStatusCode());
        var expected = getExpected(request, testData, DEFAULT_OFFSET, DEFAULT_PAGE_SIZE);
        assertEquals(expected, response.getBody());
    }

    @Test
    void shouldRetrieveManyHits() throws IOException, BadRequestException {
        var testData = new TestData(getRandomTestList());
        databaseConnection.write(GRAPH, toTriples(testData.getModel()), Lang.NTRIPLES);
        var service = new QueryService(databaseConnection);
        var handler = new FetchDataReport(service);
        var request = new TestingRequest(
            TEXT_CSV.toString(),
            "affiliation",
            "2026-01-01T03:02:11Z",
            "1998-01-01T05:09:32Z",
            "0",
            "100"
        );
        var input = generateHandlerRequest(request);
        var output = new ByteArrayOutputStream();
        handler.handleRequest(input, output, new FakeContext());
        var response = GatewayResponse.fromOutputStream(output, String.class);
        assertEquals(200, response.getStatusCode());
        var expected = getExpected(request, testData, DEFAULT_OFFSET, 100);
        assertEquals(expected, response.getBody());
    }

    private static MediaType getResponseType(TestingRequest request) {
        return TEXT_CSV.toString().equals(request.acceptHeader().get(ACCEPT))
                   ? TEXT_CSV
                   : TEXT_PLAIN;
    }

    private static InputStream generateHandlerRequest(TestingRequest request) throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(JsonUtils.dtoObjectMapper)
                   .withHeaders(request.acceptHeader())
                   .withPathParameters(request.pathParameters())
                   .withQueryParameters(request.queryParameters())
                   .build();
    }

    private static void catchExpectedExceptionsExceptHttpException(Exception e) {
        if (!(e instanceof HttpException)) {
            throw new RuntimeException(e);
        }
    }

    private List<DatePair> getRandomTestList() {
        int i = 20;
        var pairs = new ArrayList<DatePair>();
        while (i > 0) {
            pairs.add(new DatePair(new PublicationDate("2024", "02", "02"),
                                   Instant.now().minus(100, ChronoUnit.DAYS)));
            i--;
        }
        return pairs;
    }

    private String toTriples(Model model) {
        var stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, model, Lang.NTRIPLES);
        return stringWriter.toString();
    }

    // TODO: Craft queries and data to test every SELECT clause, BEFORE/AFTER/PAGE_SIZE.
    private String getExpected(TestingRequest request, TestData test, int offset, int pageSize)
        throws BadRequestException {
        var responseType = getResponseType(request);
        var before = InstantUtil.before(request.before());
        var after = InstantUtil.after(request.after());
        var publicationUrisInDatabaseOrder = queryPublicationIds(before, after);
        var data = switch (ReportType.parse(request.reportType())) {
            case AFFILIATION -> test.getAffiliationResponseData(offset, pageSize, publicationUrisInDatabaseOrder);
            case CONTRIBUTOR -> test.getContributorResponseData(offset, pageSize, publicationUrisInDatabaseOrder);
            case FUNDING -> test.getFundingResponseData(offset, pageSize, publicationUrisInDatabaseOrder);
            case IDENTIFIER -> test.getIdentifierResponseData(offset, pageSize, publicationUrisInDatabaseOrder);
            case PUBLICATION -> test.getPublicationResponseData(offset, pageSize, publicationUrisInDatabaseOrder);
            case NVI -> getNviResponseData(test, offset, pageSize, before, after);
        };

        return TEXT_CSV.equals(responseType)
                   ? data
                   : generateTable(data);
    }

    private List<String> queryPublicationIds(Instant before, Instant after) {
        return databaseConnection.getResult(generateQuery(before, after, PUBLICATION_URI_SPARQL), new RawFormatter())
                   .lines()
                   .toList();
    }

    private String getNviResponseData(TestData test, int offset, int pageSize, Instant before, Instant after) {
        var nviResultOrder = databaseConnection.getResult(
            generateQuery(before, after, "nviPublicationUri"), new RawFormatter()).lines()
                                 .map(line -> line.split(" "))
                                 .collect(Collectors.groupingBy(split -> split[0], Collectors.mapping(arr -> arr[1], Collectors.toList())));
        return test.getNviResponseData(offset, pageSize, nviResultOrder);
    }

    private Query generateQuery(Instant before, Instant after, String sparqlName) {
        var template = Path.of(TEMPLATE_DIRECTORY, sparqlName + SPARQL);
        var sparqlTemplate = IoUtils.stringFromResources(template)
                                 .replace(BEFORE_PLACEHOLDER, before.toString())
                                 .replace(AFTER_PLACEHOLDER, after.toString());
        return QueryFactory.create(sparqlTemplate);
    }
}

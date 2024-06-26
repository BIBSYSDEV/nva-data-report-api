package no.sikt.nva.data.report.api.fetch.model;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static java.util.Objects.nonNull;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import no.sikt.nva.data.report.api.fetch.utils.InstantUtil;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;

public class ReportRequest {

    private static final String TYPE_SELECTOR = "type";
    private static final String BEFORE_SELECTOR = "before";
    private static final String AFTER_SELECTOR = "after";
    private static final String OFFSET_SELECTOR = "offset";
    private static final String PAGE_SIZE_SELECTOR = "pageSize";
    private final ReportFormat reportFormat;
    private final ReportType reportType;
    private final Instant before;
    private final Instant after;
    private final int offset;
    private final int pageSize;

    public ReportRequest(ReportFormat reportFormat,
                         ReportType reportType,
                         Instant before,
                         Instant after,
                         Integer offset,
                         Integer pageSize) throws BadRequestException {

        this.reportFormat = nonNull(reportFormat) ? reportFormat : ReportFormat.CSV;
        this.reportType = reportType;
        this.before = nonNull(before) ? before : Instant.now();
        this.after = nonNull(after) ? after : Instant.now().minus(1, ChronoUnit.CENTURIES);
        this.offset = nonNull(offset) ? offset : 0;
        this.pageSize = nonNull(pageSize) ? pageSize : 10;
        validate();
    }

    private void validate() throws BadRequestException {
        if (this.offset < 0) {
            throw new BadRequestException("Offset cannot be less than zero");
        }
        if (this.pageSize < 0) {
            throw new BadRequestException("Page size cannot be less than zero");
        }
        if (this.after.isAfter(this.before)) {
            throw new BadRequestException("Logically, 'before should be after the value of 'after'");
        }
    }

    public ReportRequest(String reportFormat,
                         String reportType,
                         String before,
                         String after,
                         Integer offset,
                         Integer pageSize)
        throws BadRequestException {
        this(ReportFormat.fromMediaType(reportFormat), ReportType.parse(reportType), InstantUtil.before(before),
             InstantUtil.after(after), offset, pageSize);
    }

    public static ReportRequest fromRequestInfo(RequestInfo requestInfo) throws BadRequestException {
        return new ReportRequest(extractFormat(requestInfo),
                                 extractReportType(requestInfo),
                                 extractBeforeDate(requestInfo),
                                 extractAfterDate(requestInfo),
                                 extractOffset(requestInfo),
                                 extractPageSize(requestInfo));
    }

    public ReportFormat getReportFormat() {
        return reportFormat;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public Instant getBefore() {
        return before;
    }

    public Instant getAfter() {
        return after;
    }

    public int getOffset() {
        return offset;
    }

    public int getPageSize() {
        return pageSize;
    }


    private static Integer extractPageSize(RequestInfo requestInfo) {
        return extractValueAsInteger(requestInfo, PAGE_SIZE_SELECTOR);
    }

    private static Integer extractOffset(RequestInfo requestInfo) {
        return extractValueAsInteger(requestInfo, OFFSET_SELECTOR);
    }

    private static Integer extractValueAsInteger(RequestInfo requestInfo, String selector) {
        return requestInfo.getQueryParameterOpt(selector)
                   .map(Integer::valueOf)
                   .orElse(null);
    }

    private static String extractAfterDate(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(AFTER_SELECTOR).orElse(null);
    }

    private static String extractBeforeDate(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(BEFORE_SELECTOR).orElse(null);
    }

    private static String extractReportType(RequestInfo requestInfo) {
        return requestInfo.getPathParameter(TYPE_SELECTOR);
    }

    private static String extractFormat(RequestInfo requestInfo) {
        return requestInfo.getHeaders().getOrDefault(ACCEPT, null);
    }
}

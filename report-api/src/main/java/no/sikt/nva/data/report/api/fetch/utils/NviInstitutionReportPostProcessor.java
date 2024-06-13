package no.sikt.nva.data.report.api.fetch.utils;

import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.APPROVAL_STATUS_FUNCTION;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.CONTRIBUTOR_IDENTIFIER_FUNCTION;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.DEPARTMENT_IDENTIFIER_FUNCTION;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.FACULTY_IDENTIFIER_FUNCTION;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.GLOBAL_STATUS_FUNCTION;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.GROUP_IDENTIFIER_FUNCTION;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.INSTITUTION_IDENTIFIER_FUNCTION;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.PUBLICATION_CHANNEL_TYPE_FUNCTION;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.PUBLICATION_IDENTIFIER_FUNCTION;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.PUBLICATION_INSTANCE_FUNCTION;
import static no.sikt.nva.data.report.api.fetch.utils.PostProcessFunction.SCIENTIFIC_LEVEL_FUNCTION;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.xlsx.Excel;

public final class NviInstitutionReportPostProcessor {

    private NviInstitutionReportPostProcessor() {
    }

    public static Excel postProcess(Excel excel) {
        return excel.postProcess(
            List.of(GLOBAL_STATUS_FUNCTION,
                    PUBLICATION_IDENTIFIER_FUNCTION,
                    CONTRIBUTOR_IDENTIFIER_FUNCTION,
                    APPROVAL_STATUS_FUNCTION,
                    INSTITUTION_IDENTIFIER_FUNCTION,
                    FACULTY_IDENTIFIER_FUNCTION,
                    DEPARTMENT_IDENTIFIER_FUNCTION,
                    GROUP_IDENTIFIER_FUNCTION,
                    SCIENTIFIC_LEVEL_FUNCTION,
                    PUBLICATION_CHANNEL_TYPE_FUNCTION,
                    PUBLICATION_INSTANCE_FUNCTION));
    }
}

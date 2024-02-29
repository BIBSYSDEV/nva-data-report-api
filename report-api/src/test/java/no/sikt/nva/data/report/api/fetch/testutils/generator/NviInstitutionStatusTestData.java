package no.sikt.nva.data.report.api.fetch.testutils.generator;

import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.AUTHOR_COUNT;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.AUTHOR_INT;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.AUTHOR_SHARE_COUNT;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.CONTRIBUTOR_IDENTIFIER;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.DEPARTMENT_ID;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.FACULTY_ID;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.FIRST_NAME;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.GROUP_ID;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.INSTITUTION_APPROVAL_STATUS;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.INSTITUTION_ID;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.INTERNATIONAL_COLLABORATION_FACTOR;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.ISSN;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.IS_REPORTED;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.LANGUAGE;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.LAST_NAME;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PAGE_COUNT;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PAGE_FROM;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PAGE_TO;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.POINTS_FOR_AFFILIATION;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_LEVEL;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_NAME;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_CHANNEL_TYPE;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_INSTANCE;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_TITLE;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.PUBLICATION_TYPE_CHANNEL_LEVEL_POINTS;
import static no.sikt.nva.data.report.api.fetch.testutils.generator.NviInstitutionStatusHeaders.TOTAL_POINTS;
import java.util.List;

public final class NviInstitutionStatusTestData {

    public static final List<String> NVI_INSTITUTION_STATUS_HEADERS = List.of(INSTITUTION_APPROVAL_STATUS,
                                                                              PUBLICATION_INSTANCE,
                                                                              PUBLICATION_CHANNEL_TYPE,
                                                                              ISSN,
                                                                              PUBLICATION_CHANNEL_LEVEL,
                                                                              CONTRIBUTOR_IDENTIFIER,
                                                                              INSTITUTION_ID,
                                                                              FACULTY_ID,
                                                                              DEPARTMENT_ID,
                                                                              GROUP_ID,
                                                                              AUTHOR_COUNT,
                                                                              AUTHOR_INT,
                                                                              PUBLICATION_TYPE_CHANNEL_LEVEL_POINTS,
                                                                              LAST_NAME,
                                                                              FIRST_NAME,
                                                                              PUBLICATION_CHANNEL_NAME,
                                                                              PAGE_FROM,
                                                                              PAGE_TO,
                                                                              PAGE_COUNT,
                                                                              PUBLICATION_TITLE,
                                                                              LANGUAGE,
                                                                              IS_REPORTED,
                                                                              TOTAL_POINTS,
                                                                              INTERNATIONAL_COLLABORATION_FACTOR,
                                                                              AUTHOR_SHARE_COUNT,
                                                                              POINTS_FOR_AFFILIATION);
}

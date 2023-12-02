package no.sikt.nva.data.report.api.fetch.testutils.generator.model;

import no.sikt.nva.data.report.api.fetch.testutils.generator.Constants;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;

public interface PublicationContext {
    Property ONLINE_ISSN = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "onlineIssn");
    Property PRINT_ISSN = new PropertyImpl(Constants.ONTOLOGY_BASE_URI, "printIssn");

    Resource getSubject();

    PublicationContext withScientificValue(String value);

    PublicationContext withName(String name);
    Model build();

    PublicationContext withOnlineIssn(String onlineIssn);

    PublicationContext withPrintIssn(String printIssn);
}

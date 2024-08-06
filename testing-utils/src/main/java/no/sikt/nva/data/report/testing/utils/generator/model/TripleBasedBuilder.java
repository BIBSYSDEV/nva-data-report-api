package no.sikt.nva.data.report.testing.utils.generator.model;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

public abstract class TripleBasedBuilder {

    public static final Property TYPE = RDF.type;

    public abstract Model build();

    public abstract Resource getSubject();
}

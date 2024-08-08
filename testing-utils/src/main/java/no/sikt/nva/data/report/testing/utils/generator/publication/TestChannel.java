package no.sikt.nva.data.report.testing.utils.generator.publication;

import java.util.UUID;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.JournalGenerator;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.PublicationContext;
import no.sikt.nva.data.report.testing.utils.generator.model.publication.PublisherGenerator;

public class TestChannel {

    private String type;
    private UUID identifier;
    private String name;
    private String onlineIssn;
    private String printIssn;
    private String scientificValue;

    public TestChannel() {

    }

    public TestChannel withType(String channelType) {
        this.type = channelType;
        return this;
    }

    public TestChannel withIdentifier(UUID channelIdentifier) {
        this.identifier = channelIdentifier;
        return this;
    }

    public TestChannel withName(String channelName) {
        this.name = channelName;
        return this;
    }

    public TestChannel withOnlineIssn(String channelOnlineIssn) {
        this.onlineIssn = channelOnlineIssn;
        return this;
    }

    public TestChannel withPrintIssn(String channelPrintIssn) {
        this.printIssn = channelPrintIssn;
        return this;
    }

    public TestChannel withScientificValue(String value) {
        this.scientificValue = value;
        return this;
    }

    public PublicationContext toModel() {
        return type.contains("Journal")
                   ? new JournalGenerator(identifier)
                         .withName(name)
                         .withScientificValue(scientificValue)
                         .withOnlineIssn(onlineIssn)
                         .withPrintIssn(printIssn)
                   : new PublisherGenerator(identifier)
                         .withName(name)
                         .withScientificValue(scientificValue);
    }

    public String getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier.toString();
    }

    public String getName() {
        return name;
    }

    public String getOnlineIssn() {
        return onlineIssn;
    }

    public String getPrintIssn() {
        return printIssn;
    }

    public String getScientificValue() {
        return scientificValue;
    }
}

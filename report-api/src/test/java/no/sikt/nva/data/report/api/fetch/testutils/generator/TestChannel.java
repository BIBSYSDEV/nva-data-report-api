package no.sikt.nva.data.report.api.fetch.testutils.generator;

import java.util.UUID;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.JournalGenerator;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.PublicationContext;
import no.sikt.nva.data.report.api.fetch.testutils.generator.model.PublisherGenerator;

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
        return "Journal".equals(type)
            ? new JournalGenerator(identifier)
                  .withName(name)
                  .withScientificValue(scientificValue)
                  .withOnlineIssn(onlineIssn)
                  .withPrintIssn(printIssn)
           : new PublisherGenerator(identifier)
                 .withName(name)
                 .withScientificValue(scientificValue);
    }
}
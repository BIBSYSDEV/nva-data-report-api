package no.sikt.nva.data.report.api.etl.queue;

import nva.commons.core.JacocoGenerated;

public record MessageResponse(String messageId) {

    @Override
    @JacocoGenerated
    public String toString() {
        return "MessageResponse{" +
               "messageId='" + messageId + '\'' +
               '}';
    }
}

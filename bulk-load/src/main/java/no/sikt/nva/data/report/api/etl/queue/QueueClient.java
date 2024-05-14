package no.sikt.nva.data.report.api.etl.queue;

public interface QueueClient {

    MessageResponse sendMessage(String body);
}

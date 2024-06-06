package no.sikt.nva.data.report.api.fetch.queue;

public interface QueueClient {

    MessageResponse sendMessage(String body);
}
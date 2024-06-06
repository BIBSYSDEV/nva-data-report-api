package no.sikt.nva.data.report.api.fetch;

import java.util.ArrayList;
import java.util.List;
import no.sikt.nva.data.report.api.fetch.queue.MessageResponse;
import no.sikt.nva.data.report.api.fetch.queue.QueueClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class FakeSqsClient implements QueueClient {

    private final List<SendMessageRequest> sentMessages = new ArrayList<>();

    public List<SendMessageRequest> getSentMessages() {
        return sentMessages;
    }

    @Override
    public MessageResponse sendMessage(String body) {
        var messageRequest = SendMessageRequest.builder().messageBody(body).build();
        sentMessages.add(messageRequest);
        return new MessageResponse("fakeMessageId");
    }

    public void removeSentMessages() {
        sentMessages.clear();
    }
}
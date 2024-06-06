package no.sikt.nva.data.report.api.fetch.aws;

import java.time.Duration;
import no.sikt.nva.data.report.api.fetch.queue.MessageResponse;
import no.sikt.nva.data.report.api.fetch.queue.QueueClient;
import nva.commons.core.JacocoGenerated;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@JacocoGenerated
public class AwsSqsClient implements QueueClient {

    private static final int MAX_CONNECTIONS = 10_000;
    private static final int IDLE_TIME = 30;
    private static final int TIMEOUT_TIME = 30;
    private final String queueUrl;
    private final SqsClient sqsClient;

    public AwsSqsClient(Region region, String queueUrl) {
        this.sqsClient = defaultSqsClient(region);
        this.queueUrl = queueUrl;
    }

    @Override
    public MessageResponse sendMessage(String body) {
        var messageRequest = SendMessageRequest.builder().messageBody(body).queueUrl(queueUrl).build();
        var response = sqsClient.sendMessage(messageRequest);
        return new MessageResponse(response.messageId());
    }

    protected static SqsClient defaultSqsClient(Region region) {
        return SqsClient.builder()
                   .region(region)
                   .httpClient(httpClientForConcurrentQueries())
                   .build();
    }

    private static SdkHttpClient httpClientForConcurrentQueries() {
        return ApacheHttpClient.builder()
                   .useIdleConnectionReaper(true)
                   .maxConnections(MAX_CONNECTIONS)
                   .connectionMaxIdleTime(Duration.ofSeconds(IDLE_TIME))
                   .connectionTimeout(Duration.ofSeconds(TIMEOUT_TIME))
                   .build();
    }
}
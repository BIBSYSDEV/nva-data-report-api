package no.sikt.nva.data.report.testing.utils;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import java.util.List;

public final class QueueServiceTestUtils {

    private QueueServiceTestUtils() {
    }

    public static SQSEvent createEvent(String body) {
        var sqsEvent = new SQSEvent();
        var message = createMessage(body);
        sqsEvent.setRecords(List.of(message));
        return sqsEvent;
    }

    private static SQSMessage createMessage(String body) {
        var message = new SQSMessage();
        message.setBody(body);
        return message;
    }
}

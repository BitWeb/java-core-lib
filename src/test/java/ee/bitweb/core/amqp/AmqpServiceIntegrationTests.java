package ee.bitweb.core.amqp;

import ee.bitweb.core.app.amqp.util.AmqpParsedMessage;
import ee.bitweb.core.app.amqp.util.AmqpTestHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "ee.bitweb.core.amqp.auto-configuration=true")
@ActiveProfiles("AmqpTest")
class AmqpServiceIntegrationTests {

    @Autowired
    private AmqpTestHelper helper;

    @Autowired
    private AmqpService service;

    @Test
    void sendingMessageShouldLandItInQueue() throws Exception {
        Queue queue = helper.createQueue();

        service.sendMessage(queue.getName(), new Payload("some-data"));
        AmqpParsedMessage<Payload> message = helper.convert(
                helper.waitAndGetResponse(queue.getName(), 1),
                Payload.class
        ).get(0);
        helper.waitForEmptyQueue(queue.getName());

        assertEquals("some-data", message.getBody().getData());
    }

    @Test
    void sendingWithReplyHeaderShouldCreateTheHeaderAndPutMessageToQueue() throws Exception {
        Queue queue = helper.createQueue();

        service.sendMessage(queue.getName(), "some-response-queue-name", new Payload("some-data"));

        AmqpParsedMessage<Payload> response = helper.convert(
                helper.waitAndGetResponse(queue.getName(), 1),
                Payload.class
        ).get(0);
        helper.waitForEmptyQueue(queue.getName());

        assertEquals("some-response-queue-name", response.getMessage().getMessageProperties().getReplyTo());
        assertEquals("some-data", response.getBody().getData());
    }

    @Test
    void sendingASignalMessageWithResponseHeaderShouldPutAMessageToQueue() throws Exception {
        Queue queue = helper.createQueue();

        service.sendMessage(queue.getName(), "some-response-queue-name");

        AmqpParsedMessage<String> response = helper.convert(
                helper.waitAndGetResponse(queue.getName(), 1),
                String.class
        ).get(0);
        helper.waitForEmptyQueue(queue.getName());

        assertEquals("some-response-queue-name", response.getMessage().getMessageProperties().getReplyTo());
        assertEquals("", response.getBody());
    }


    @Test
    void sendingASignalMessageWithInterceptorAndResponseHeaderShouldPutAMessageToQueue() throws Exception {
        Queue queue = helper.createQueue();

        service.sendMessage(
                queue.getName(),
                "some-response-queue-name",
                m -> {
                    m.getMessageProperties().setHeader("custom-header", "custom-value");

                    return m;
                }
        );

        AmqpParsedMessage<String> response = helper.convert(
                helper.waitAndGetResponse(queue.getName(), 1),
                String.class
        ).get(0);
        helper.waitForEmptyQueue(queue.getName());

        assertEquals("some-response-queue-name", response.getMessage().getMessageProperties().getReplyTo());
        assertEquals("custom-value", response.getMessage().getMessageProperties().getHeader("custom-header"));
        assertEquals("", response.getBody());
    }


    @Test
    void sendingAMessageWithInterceptorShouldCallInterceptorAndPutMessageToQueue() throws Exception {
        Queue queue = helper.createQueue();

        service.sendMessage(queue.getName(), new Payload("some-data"), m -> {
            m.getMessageProperties().setHeader("custom-header", "custom-value");

            return m;
        });

        AmqpParsedMessage<Payload> message = helper.convert(
                helper.waitAndGetResponse(queue.getName(), 1),
                Payload.class
        ).get(0);

        helper.waitForEmptyQueue(queue.getName());

        assertEquals("custom-value", message.getMessage().getMessageProperties().getHeader("custom-header"));
        assertEquals("some-data", message.getBody().getData());
    }

    @Test
    void sendingAMessageWithResponseQueueAndInterceptorPutsResponseHeaderAndCallsIntercetorAndPutsMessageToQueue() throws Exception {
        Queue queue = helper.createQueue();

        service.sendMessage(
                queue.getName(),
                "some-response-queue-name",
                new Payload("some-data"),
                m -> {
                    m.getMessageProperties().setHeader("custom-header", "custom-value");

                    return m;
                }
        );

        AmqpParsedMessage<Payload> message = helper.convert(
                helper.waitAndGetResponse(queue.getName(), 1),
                Payload.class
        ).get(0);

        helper.waitForEmptyQueue(queue.getName());

        assertEquals("some-response-queue-name", message.getMessage().getMessageProperties().getReplyTo());
        assertEquals("custom-value", message.getMessage().getMessageProperties().getHeader("custom-header"));
        assertEquals("some-data", message.getBody().getData());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Payload {
        private String data;
    }
}

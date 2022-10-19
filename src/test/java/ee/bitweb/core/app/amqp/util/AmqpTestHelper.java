package ee.bitweb.core.app.amqp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ee.bitweb.core.amqp.AmqpService;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(value = "test.listener.enabled", havingValue = "true")
public class AmqpTestHelper {

    @Autowired
    private AmqpAdmin admin;

    @Autowired
    private AmqpService amqpService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    void init() {
        mapper.registerModule(new JavaTimeModule());
    }

    public Queue createQueue() {
        return admin.declareQueue();
    }

    public void waitForResponse(String responseQueue, int size ){
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Integer count = (Integer) admin.getQueueProperties(responseQueue).get("QUEUE_MESSAGE_COUNT");
            return count >= size;
        });
    }

    public void waitForEmptyQueue(String queueName) {
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Integer count = (Integer) admin.getQueueProperties(queueName).get("QUEUE_MESSAGE_COUNT");
            return count == 0;
        });
    }

    public void clear(String queueName) {
        admin.purgeQueue(queueName);
    }

    public void sendMessage(String targetQueue, Message message) {
        rabbitTemplate.send(targetQueue, message);
    }

    public void sendMessage(String targetQueue, String responseQueue, AmqpService.MessageInterceptor interceptor) {
        amqpService.sendMessage(targetQueue, responseQueue, interceptor);
    }

    public void sendMessage(String targetQueue, String responseQueue, Object message, AmqpService.MessageInterceptor interceptor) {
        amqpService.sendMessage(targetQueue, responseQueue, message, interceptor);
    }

    public void sendMessage(String targetQueue, Object payload) {
        amqpService.sendMessage(targetQueue, payload);
    }

    public void sendMessage(String targetQueue, String responseQueue, Object message) {
        amqpService.sendMessage(targetQueue, responseQueue, message);
    }

    public List<Message> waitAndGetResponse(String responseQueue, int size) {
        waitForResponse(responseQueue, size);

        return getResponse(responseQueue, size);
    }

    public List<Message> getResponse(String responseQueue, int size) {
        List<Message> result = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Message message = rabbitTemplate.receive(responseQueue);

            result.add(message);
        }

        return result;
    }

    public <T> AmqpParsedMessage<T> convert(Message message, Class<T> clazz) throws IOException {
        return new AmqpParsedMessage<T>(message, mapper.readValue(new String(message.getBody()), clazz));
    }

    public <T> List<AmqpParsedMessage<T>> convert(List<Message> messages, Class<T> clazz) throws IOException {
        List<AmqpParsedMessage<T>> response = new ArrayList<>();
        for (Message message : messages) {
            response.add(convert(message, clazz));
        }
        return response;
    }

    public Properties getQueueProperties(String queueName) {
        return admin.getQueueProperties(queueName);
    }
 }

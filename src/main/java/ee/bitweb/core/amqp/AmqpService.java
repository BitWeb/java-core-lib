package ee.bitweb.core.amqp;

import ee.bitweb.core.exception.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@RequiredArgsConstructor
public class AmqpService {

    private final RabbitTemplate template;

    public void sendMessage(String targetQueue, String responseQueue, Object message) {
        send(
                () -> template.convertAndSend(targetQueue, message, m -> {
                        m.getMessageProperties().setReplyTo(responseQueue);

                        return m;
                })
        );
    }

    public void sendMessage(String targetQueue, String responseQueue, Object message, MessageInterceptor interceptor) {
        send(
                () -> template.convertAndSend(targetQueue, message, m -> {
                        m.getMessageProperties().setReplyTo(responseQueue);
                        interceptor.process(m);

                        return m;
                })
        );
    }

    public void sendMessage(String targetQueue, String responseQueue) {
        send(
                () -> template.convertAndSend(targetQueue, "", m -> {
                        m.getMessageProperties().setReplyTo(responseQueue);

                        return m;
                })
        );
    }

    public void sendMessage(String targetQueue, String responseQueue, MessageInterceptor interceptor) {
        send(
                () -> template.convertAndSend(targetQueue, "", m -> {
                        m.getMessageProperties().setReplyTo(responseQueue);
                        interceptor.process(m);

                        return m;
                })
        );
    }

    public void sendMessage(String targetQueue, Object message) {
        send(() -> template.convertAndSend(targetQueue, message, m -> m));
    }

    public void sendMessage(String targetQueue, Object message, MessageInterceptor interceptor) {
        send(
                () -> template.convertAndSend(targetQueue, message, m -> {
                        interceptor.process(m);

                        return m;
                })
        );
    }

    private void send(SendingInvocation invocation) {
        try {
            invocation.invoke();
        } catch (Exception e) {
            throw new CoreException("Sending message to MQ failed", e);
        }
    }

    @FunctionalInterface
    public interface MessageInterceptor {
        Message process(Message message);
    }

    @FunctionalInterface
    private interface  SendingInvocation {
        void invoke();
    }
}

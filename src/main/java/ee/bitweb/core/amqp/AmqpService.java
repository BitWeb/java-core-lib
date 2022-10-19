package ee.bitweb.core.amqp;

import ee.bitweb.core.exception.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@RequiredArgsConstructor
public class AmqpService {

    private final RabbitTemplate template;

    public void sendMessage(String targetQueue, String responseQueue, Object message) {
        try {
            template.convertAndSend(targetQueue, message, m -> {
                m.getMessageProperties().setReplyTo(responseQueue);

                return m;
            });

        } catch (Exception e) {
            throwCoreException(e);
        }
    }

    public void sendMessage(String targetQueue, String responseQueue, Object message, MessageInterceptor interceptor) {
        try {
            template.convertAndSend(targetQueue, message, m -> {
                m.getMessageProperties().setReplyTo(responseQueue);
                interceptor.process(m);

                return m;
            });

        } catch (Exception e) {
            throwCoreException(e);
        }
    }

    public void sendMessage(String targetQueue, String responseQueue) {
        try {
            template.convertAndSend(targetQueue, "", m -> {
                m.getMessageProperties().setReplyTo(responseQueue);

                return m;
            });

        } catch (Exception e) {
            throwCoreException(e);
        }
    }

    public void sendMessage(String targetQueue, String responseQueue, MessageInterceptor interceptor) {
        try {
            template.convertAndSend(targetQueue, "", m -> {
                m.getMessageProperties().setReplyTo(responseQueue);
                interceptor.process(m);

                return m;
            });

        } catch (Exception e) {
            throwCoreException(e);
        }
    }

    public void sendMessage(String targetQueue, Object message) {
        try {
            template.convertAndSend(targetQueue, message, m -> m);
        } catch (Exception e) {
            throwCoreException(e);
        }
    }

    public void sendMessage(String targetQueue, Object message, MessageInterceptor interceptor) {
        try {
            template.convertAndSend(targetQueue, message, m -> {
                interceptor.process(m);

                return m;
            });
        } catch (Exception e) {
            throwCoreException(e);
        }
    }

    private void throwCoreException(Exception e) {
        throw new CoreException(
                String.format(
                        "Sending message failed to MQ: %s",
                        e.getMessage()
                )
        );
    }

    @FunctionalInterface
    public interface MessageInterceptor {
        String process(Message message);
    }
}

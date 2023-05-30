package ee.bitweb.core.amqp;

import org.springframework.amqp.core.MessagePostProcessor;

public interface AmqpBeforePublishMessageProcessor extends MessagePostProcessor {
}

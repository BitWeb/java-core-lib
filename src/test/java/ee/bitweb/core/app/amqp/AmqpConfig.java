package ee.bitweb.core.app.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "ee.bitweb.core.amqp.auto-configuration", havingValue = "true")
public class AmqpConfig {

    public static final String COMMAND_QUEUE_NAME = "command";
    public static final String COMMAND_QUEUE_SIMPLE_NAME = "command-simple";
    public static final String COMMAND_DEAD_LETTER_EXCHANGE_NAME = "command-dlx";
    public static final String RESPONSE_QUEUE_NAME = "response";

    @Bean
    public Queue commandDlxQueue() {
        return QueueBuilder
                .nonDurable(COMMAND_DEAD_LETTER_EXCHANGE_NAME)
                .build();
    }

    @Bean
    public Queue responseQueue() {
        return QueueBuilder
                .nonDurable(RESPONSE_QUEUE_NAME)
                .build();
    }

    @Bean
    public Queue commandQueue() {
        return QueueBuilder
                .nonDurable(COMMAND_QUEUE_NAME)
                .deadLetterExchange("")
                .deadLetterRoutingKey(COMMAND_DEAD_LETTER_EXCHANGE_NAME)
                .build();
    }

    @Bean Queue commandSimpleQueue() {
        return QueueBuilder
                .nonDurable(COMMAND_QUEUE_SIMPLE_NAME)
                .build();
    }
}

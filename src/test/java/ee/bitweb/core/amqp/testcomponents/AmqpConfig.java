package ee.bitweb.core.amqp.testcomponents;

import ee.bitweb.core.amqp.AmqpBeforePublishMessageProcessor;
import ee.bitweb.core.amqp.AmqpListenerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Order;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
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

    @Bean
    public Queue commandSimpleQueue() {
        return QueueBuilder
                .nonDurable(COMMAND_QUEUE_SIMPLE_NAME)
                .build();
    }

    @Bean
    public TestAmqpPostProcessor testAmqpPostProcessor() {
        log.debug("Create TestAmqpPostProcessor bean.");
        return new TestAmqpPostProcessor();
    }

    @Bean
    public TestAmqpListenerInterceptor testAmqpListenerInterceptor() {
        log.debug("Create TestAmqpListenerInterceptor bean.");
        return new TestAmqpListenerInterceptor();
    }

    public static class TestAmqpPostProcessor implements AmqpBeforePublishMessageProcessor {

        @Override
        public Message postProcessMessage(Message message) throws AmqpException {
            log.debug("Post process message, TestAmqpPostProcessor");
            message.getMessageProperties().setHeader("post-process-message-parameter", "some-parameter-value");

            return message;
        }
    }

    @Order(1)
    public static class TestAmqpListenerInterceptor implements AmqpListenerInterceptor {

        @Nullable
        @Override
        public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
            log.debug("Intercept message, TestAmqpListenerInterceptor");

            for (Object argument : invocation.getArguments()) {
                if (argument instanceof Message) {
                    log.debug("Process message, TestAmqpListenerInterceptor");
                    MDC.put(
                            "post-process-message-parameter",
                            ((Message) argument).getMessageProperties().getHeader("post-process-message-parameter")
                    );
                }
            }
            try {
                return invocation.proceed();
            } finally {
                log.debug("Clear MDC, TestAmqpListenerInterceptor");
                MDC.remove("post-process-message-parameter");
            }
        }
    }
}

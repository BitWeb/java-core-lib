package ee.bitweb.core.amqp.testcomponents;

import ee.bitweb.core.amqp.AmqpService;
import ee.bitweb.core.exception.CoreException;
import ee.bitweb.core.trace.context.TraceIdContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@ConditionalOnProperty(value = "test.listener.enabled", havingValue = "true")
@RequiredArgsConstructor
public class TestAmqpMessageListener {

    private final AmqpService amqpService;
    private final TraceIdContext context;

    @PostConstruct
    public void init() {
        log.info("Created a listener");
    }

    @RabbitListener(queues = {AmqpConfig.COMMAND_QUEUE_NAME, AmqpConfig.COMMAND_QUEUE_SIMPLE_NAME})
    public void onMessage(Command command) {
        log.info("Got a message");
        if (command.getThrowError()) throw new CoreException("Command told me to throw error, so I did");

        amqpService.sendMessage(
                AmqpConfig.RESPONSE_QUEUE_NAME,
                new Response(context.get(), MDC.get("post-process-message-parameter"))
        );
    }
}

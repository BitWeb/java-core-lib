package ee.bitweb.core.trace.invoker.amqp;

import ee.bitweb.core.amqp.AmqpBeforePublishMessageProcessor;
import ee.bitweb.core.trace.context.TraceIdContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;

@Slf4j
@RequiredArgsConstructor
public class AmqpTraceBeforePublishMessageProcessor implements AmqpBeforePublishMessageProcessor {

    private final AmqpTraceProperties properties;
    private final TraceIdContext context;

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        log.debug("Adding trace id  {} to AMQP message header {} ", context.get(), properties.getHeaderName());

        message.getMessageProperties().setHeader(properties.getHeaderName(), context.get());

        return message;
    }
}

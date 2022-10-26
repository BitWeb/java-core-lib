package ee.bitweb.core.trace.invoker.amqp;


import ee.bitweb.core.trace.context.TraceIdContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
public class AmqpTraceAfterReceiveMessageProcessor implements MessagePostProcessor {

    private final TraceIdContext traceIdContext;

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        log.debug("Message has been processed, clearing trace id context.");
        traceIdContext.clear();

        return message;
    }
}

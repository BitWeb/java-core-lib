package ee.bitweb.core.trace.invoker.amqp;

import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;

@RequiredArgsConstructor
public class AmqpTraceIdResolver {

    private final AmqpTraceProperties properties;
    private final TraceIdCreator creator;
    private final TraceIdContext context;

    public String resolve(Message message) {
        context.clearTraceId();
        String traceId = creator.generate(message.getMessageProperties().getHeader(properties.getHeaderName()));
        context.set(traceId);

        return traceId;
    }
}

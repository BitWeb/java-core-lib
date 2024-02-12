package ee.bitweb.core.trace.invoker.amqp;

import ee.bitweb.core.trace.context.TraceIdContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;

@Slf4j
public class AmqpTraceAwareExceptionHandler extends ConditionalRejectingErrorHandler {

    private final TraceIdContext context;

    public AmqpTraceAwareExceptionHandler(TraceIdContext context, FatalExceptionStrategy strategy) {
        super(strategy);
        this.context = context;
    }

    @Override
    public void handleError(Throwable t) {
        try {
            super.handleError(t);
        } finally {
            context.clear();
        }
    }
}

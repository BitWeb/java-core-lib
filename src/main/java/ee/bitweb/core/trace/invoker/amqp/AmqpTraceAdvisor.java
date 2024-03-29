package ee.bitweb.core.trace.invoker.amqp;

import ee.bitweb.core.amqp.AmqpListenerInterceptor;
import ee.bitweb.core.trace.context.TraceIdContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.amqp.core.Message;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class AmqpTraceAdvisor implements AmqpListenerInterceptor {

    private final AmqpTraceIdResolver resolver;
    private final TraceIdContext context;
    private final boolean clearContextOnException;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        log.debug("Attempting to resolve trace id from Incoming message.");

        for (Object argument : invocation.getArguments()) {
            if (argument instanceof Message) {
                log.debug("Found Message object argument list, invoking trace resolution.");
                resolver.resolve((Message) argument);
            }
        }
        try {
            Object result = invocation.proceed();
            context.clear();

            return result;
        } catch (Throwable t) {
            if (clearContextOnException) {
                context.clear();
            }
            throw t;
        }
    }
}

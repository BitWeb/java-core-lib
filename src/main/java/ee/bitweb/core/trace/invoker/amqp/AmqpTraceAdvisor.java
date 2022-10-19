package ee.bitweb.core.trace.invoker.amqp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.amqp.core.Message;

@Slf4j
@RequiredArgsConstructor
public class AmqpTraceAdvisor implements MethodInterceptor {

    private final AmqpTraceIdResolver resolver;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        log.debug("Attempting to resolve trace id from Incoming message.");

        for (Object argument : invocation.getArguments()) {
            if (argument instanceof Message) {
                log.debug("Found Message object argument list, invoking trace resolution.");
                resolver.resolve((Message) argument);
            }
        }

        return invocation.proceed();
    }

}

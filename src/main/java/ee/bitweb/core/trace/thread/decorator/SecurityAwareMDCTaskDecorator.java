package ee.bitweb.core.trace.thread.decorator;

import ee.bitweb.core.trace.thread.ThreadTraceIdResolver;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class SecurityAwareMDCTaskDecorator implements TaskDecorator {

    private final ThreadTraceIdResolver resolver;

    @Override
    public Runnable decorate(Runnable runnable) {
        final Map<String, String> contextMap = MDC.getCopyOfContextMap();
        final SecurityContext securityContext = SecurityContextHolder.getContext();

        return () -> {
            try {
                MDC.setContextMap(contextMap == null ? new HashMap<>() : contextMap);
                SecurityContextHolder.setContext(securityContext == null ? SecurityContextHolder.createEmptyContext() : securityContext);
                resolver.resolve();

                runnable.run();
            } finally {
                MDC.clear();
                SecurityContextHolder.clearContext();
            }
        };
    }
}

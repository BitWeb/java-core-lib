package ee.bitweb.core.trace.thread;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

@RequiredArgsConstructor
public class MDCTaskDecorator implements TaskDecorator {

    private final ThreadTraceIdResolver resolver;

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        var securityContext = SecurityContextHolder.getContext();

        return () -> {
            try {
                MDC.setContextMap(contextMap);
                SecurityContextHolder.setContext(securityContext);
                resolver.resolve();

                runnable.run();
            } finally {
                MDC.clear();
                SecurityContextHolder.clearContext();
            }
        };
    }
}

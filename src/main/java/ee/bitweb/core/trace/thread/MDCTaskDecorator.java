package ee.bitweb.core.trace.thread;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * @deprecated use BasicMDCTaskDecorator or SecurityAwareMDCTaskDecorator
 */
@Deprecated(since = "3.3.0", forRemoval = true)
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

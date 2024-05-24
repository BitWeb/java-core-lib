package ee.bitweb.core.trace.thread.decorator;

import ee.bitweb.core.trace.thread.ThreadTraceIdResolver;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

@RequiredArgsConstructor
public class BasicMDCTaskDecorator implements TaskDecorator {

    private final ThreadTraceIdResolver resolver;

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try {
                MDC.setContextMap(contextMap);
                resolver.resolve();

                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}

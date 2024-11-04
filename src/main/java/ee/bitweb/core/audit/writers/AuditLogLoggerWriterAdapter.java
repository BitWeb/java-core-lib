package ee.bitweb.core.audit.writers;

import ee.bitweb.core.audit.AuditLogFilter;
import ee.bitweb.core.audit.mappers.*;
import java.util.Collections;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class AuditLogLoggerWriterAdapter implements AuditLogWriteAdapter {

    public static final String LOGGER_NAME = "AuditLogger";
    public static final String AUDIT_KEY = "audit";

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LOGGER_NAME);

    private List<String> debugKeys = List.of(
            ResponseBodyMapper.KEY,
            RequestBodyMapper.KEY
    );

    @Override
    public void write(Map<String, String> container) {
        Map<String, String> currentContext = MDC.getCopyOfContextMap();

        container.put(AUDIT_KEY, "1");

        Map<String, String > debug = DebugContainerExtractor.extract(container, debugKeys);

        log(container);
        logDebug(debug);

        if (currentContext != null) {
            MDC.setContextMap(currentContext);
        } else {
            MDC.clear();
        }
    }

    private void log(Map<String, String> container) {
        MDC.setContextMap(container);
        log.info(
                "Method({}),  URL({}) Status({}) ResponseSize({}) Duration({} ms)",
                get(container, RequestMethodMapper.KEY),
                get(container, RequestUrlDataMapper.KEY),
                get(container, ResponseStatusMapper.KEY),
                get(container, ResponseBodyMapper.KEY),
                get(container, AuditLogFilter.DURATION_KEY));
    }

    private void logDebug(Map<String, String> container) {
        if (!log.isDebugEnabled() || container.isEmpty()) {
            return;
        }
        MDC.setContextMap(container);
        log.info("Debug audit log");
    }

    private String get(Map<String, String> container, String key) {
        if (container.containsKey(key)) {
            return container.get(key);
        }

        return "-";
    }

    @NoArgsConstructor(access = AccessLevel.NONE)
    public static class DebugContainerExtractor {

        private static boolean hasSecondaryEntry(Map<String, String> container, List<String> keys) {
            for (String key : keys) {
                if (container.containsKey(key)) {
                    return true;
                }
            }

            return false;
        }

        public static Map<String, String> extract(Map<String, String> container, List<String> keys) {
            if (!hasSecondaryEntry(container, keys)) {
                return Collections.emptyMap();
            }

            Map<String, String> debug = new HashMap<>();

            for (String key : keys) {
                if (container.containsKey(key)) {
                    debug.put(key, container.get(key));
                    if (debug.containsKey(key)) {
                        String content = container.get(key);
                        container.put(
                                key,
                                String.valueOf(StringUtils.hasText(content) ? content.length() : 0)
                        );
                    }
                }
            }

            if (container.containsKey(TraceIdMapper.KEY)) {
                debug.put(TraceIdMapper.KEY, container.get(TraceIdMapper.KEY));
            }
            debug.put(AUDIT_KEY, "1");

            return debug;
        }
    }
}

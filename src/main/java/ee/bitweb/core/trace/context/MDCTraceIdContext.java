package ee.bitweb.core.trace.context;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public final class MDCTraceIdContext implements TraceIdContext{

    public static final String DEFAULT_KEY = "trace_id";

    private final String key;

    public static MDCTraceIdContext standard() {return new MDCTraceIdContext(DEFAULT_KEY);}

    public void set(String traceId) {
        String existing = get();

        if (!StringUtils.hasText(existing) || traceId.startsWith(existing)) {
            MDC.put(key, traceId);
        } else {
            throw new IllegalStateException("Overriding existing trace id is prohibited, appending is allowed");
        }
    }

    public String get() {
        return MDC.get(key);
    }

    @Override
    public void clear() {
        MDC.clear();
    }

    @Override
    public void put(String key, String value) {
        MDC.put(key, value);
    }
}

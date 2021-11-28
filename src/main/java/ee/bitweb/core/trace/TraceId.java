package ee.bitweb.core.trace;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TraceId {

    public static final String MDC = "trace_id";

    public static String get() {
        return org.slf4j.MDC.get(MDC);
    }

    static void set(String generated) {
        org.slf4j.MDC.put(MDC, generated);
    }
}

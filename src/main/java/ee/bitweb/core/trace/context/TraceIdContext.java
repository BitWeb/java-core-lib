package ee.bitweb.core.trace.context;

public interface TraceIdContext {

    void set(String traceId);
    String get();
    void clear();
    default void put(String key, String value) {
        throw new UnsupportedOperationException("TraceIdContext.put() has not been implemented for this kind of context.");
    }
}

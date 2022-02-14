package ee.bitweb.core.trace.context;

public interface TraceIdContext {

    void set(String traceId);
    String get();
    void clear();
}

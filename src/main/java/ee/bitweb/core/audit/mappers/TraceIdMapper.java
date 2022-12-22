package ee.bitweb.core.audit.mappers;

import ee.bitweb.core.trace.context.MDCTraceIdContext;
import ee.bitweb.core.trace.context.TraceIdContext;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class TraceIdMapper implements AuditLogDataMapper {

    public static final String KEY = MDCTraceIdContext.DEFAULT_KEY;

    private final TraceIdContext context;

    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        return context.get();
    }

    public String getKey() {
        return KEY;
    }
}

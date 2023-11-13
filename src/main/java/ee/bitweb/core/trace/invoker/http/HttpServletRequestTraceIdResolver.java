package ee.bitweb.core.trace.invoker.http;

import ee.bitweb.core.trace.creator.TraceIdCreator;
import ee.bitweb.core.trace.context.TraceIdContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class HttpServletRequestTraceIdResolver {

    private final String headerName;
    private final TraceIdContext context;
    private final TraceIdCreator creator;

    public String resolve(HttpServletRequest request) {
        String traceId = creator.generate(request.getHeader(headerName));
        context.set(traceId);

        return traceId;
    }
}

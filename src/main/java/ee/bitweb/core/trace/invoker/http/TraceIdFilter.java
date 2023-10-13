package ee.bitweb.core.trace.invoker.http;

import ee.bitweb.core.trace.context.TraceIdContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import java.io.IOException;

@Slf4j
@Order(Integer.MIN_VALUE + 20)
@RequiredArgsConstructor
public class TraceIdFilter implements Filter {

    private final TraceIdFilterConfig configuration;
    private final TraceIdContext context;
    private final HttpServletRequestTraceIdResolver resolver;

    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            resolver.resolve(httpServletRequest);

            if (response instanceof HttpServletResponse httpResponse) {
                httpResponse.addHeader(configuration.getHeaderName(), context.get());
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            context.clear();
        }
    }
}

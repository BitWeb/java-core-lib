package ee.bitweb.core.audit;

import ee.bitweb.core.audit.mappers.AuditLogDataMapper;
import ee.bitweb.core.audit.writers.AuditLogWriteAdapter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Order(Integer.MIN_VALUE + 21)
@RequiredArgsConstructor
public class AuditLogFilter implements Filter {

    public static final String DURATION_KEY = "duration";

    private final AuditLogProperties properties;
    private final List<AuditLogDataMapper> mappers;
    private final AuditLogWriteAdapter writer;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            if (isBlacklisted((HttpServletRequest) request)) {
                log.debug("Request is blacklisted for request logging, will skip further processing.");
                chain.doFilter(request, response);

                return;
            }

            doInternal(request, response, chain);
        }
    }

    private void doInternal(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(
                (HttpServletRequest) request
        );
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(
                (HttpServletResponse)  response
        );

        chain.doFilter(requestWrapper, responseWrapper);

        Map<String, String> dataContainer = new HashMap<>();

        for (AuditLogDataMapper mapper: mappers) {
            mapper.map(requestWrapper, responseWrapper, dataContainer);
        }
        responseWrapper.copyBodyToResponse();

        if (properties.isIncludeDuration()) {
            dataContainer.put(DURATION_KEY, String.valueOf(System.currentTimeMillis() - start));
        }
        try {
            writer.write(dataContainer);
        } catch (Exception e) {
            log.error("Error occured while writing to audit log", e);
        }
    }

    private boolean isBlacklisted(HttpServletRequest request) {
        for (String blacklistedUrl : properties.getBlacklist()) {
            if (request.getRequestURI().contains(blacklistedUrl)) {
                return true;
            }
        }

        return false;
    }
}

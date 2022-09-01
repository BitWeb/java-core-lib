package ee.bitweb.core.trace.invoker.http;

import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.util.HttpForwardedHeaderParser;
import ee.bitweb.core.util.HttpForwardedHeaderParser.ForwardedExtension;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Order(Integer.MIN_VALUE + 20)
@RequiredArgsConstructor
public class TraceIdFilter implements Filter {

    public static final String PATH = "path";
    public static final String URL = "url";
    public static final String METHOD = "method";
    public static final String QUERY_STRING = "query_string";
    public static final String USER_AGENT = "user_agent";
    public static final String X_FORWARDED_FOR = "x_forwarded_for";
    public static final String FORWARDED = "forwarded";
    public static final String FORWARDED_BY = "forwarded_by";
    public static final String FORWARDED_FOR = "forwarded_for";
    public static final String FORWARDED_HOST = "forwarded_host";
    public static final String FORWARDED_PROTO = "forwarded_proto";
    public static final String FORWARDED_EXTENSIONS = "forwarded_extensions";

    private static final String USER_AGENT_MISSING = "MISSING";
    private static final String FORWARDED_HEADER = "Forwarded";
    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final TraceIdFilterConfig configuration;
    private final TraceIdContext context;
    private final HttpServletRequestTraceIdResolver resolver;

    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;

            resolver.resolve(httpServletRequest);

            addPathIfEnabled(httpServletRequest);
            addUrlIfEnabled(httpServletRequest);
            addMethodIfEnabled(httpServletRequest);
            addQueryStringIfEnabled(httpServletRequest);
            addForwardingInfoIfEnabled(httpServletRequest);
            addAdditionalHeaders(httpServletRequest);
            addUserAgentIfEnabled(httpServletRequest);

            if (response instanceof HttpServletResponse) {
                ((HttpServletResponse) response).addHeader(configuration.getHeaderName(), context.get());
            }

            logAllHeaders(httpServletRequest);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            context.clear();
        }
    }

    private void addPathIfEnabled(HttpServletRequest httpServletRequest) {
        if (configuration.getEnabledFeatures().contains(Feature.ADD_PATH)) {
            context.put(PATH, httpServletRequest.getServletPath());
        }
    }

    private void addUrlIfEnabled(HttpServletRequest httpServletRequest) {
        if (configuration.getEnabledFeatures().contains(Feature.ADD_URL)) {
            context.put(URL, getUrl(httpServletRequest));
        }
    }

    private void addMethodIfEnabled(HttpServletRequest httpServletRequest) {
        if (configuration.getEnabledFeatures().contains(Feature.ADD_METHOD)) {
            context.put(METHOD, httpServletRequest.getMethod());
        }
    }

    private void addQueryStringIfEnabled(HttpServletRequest httpServletRequest) {
        if (configuration.getEnabledFeatures().contains(Feature.ADD_QUERY_STRING)) {
            context.put(QUERY_STRING, httpServletRequest.getQueryString());
        }
    }

    private void addUserAgentIfEnabled(HttpServletRequest httpServletRequest) {
        if (configuration.getEnabledFeatures().contains(Feature.ADD_USER_AGENT)) {
            context.put(USER_AGENT, getUserAgent(httpServletRequest));
        }
    }

    String getUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString == null) return url;

        return url + "?" + queryString;
    }

    String getUserAgent(HttpServletRequest httpServletRequest) {
        String header = httpServletRequest.getHeader(HttpHeaders.USER_AGENT);

        return header != null ? header : USER_AGENT_MISSING;
    }

    void addForwardingInfoIfEnabled(HttpServletRequest request) {
        if (!configuration.getEnabledFeatures().contains(Feature.ADD_FORWARDED_FOR_DATA)) return;

        String forwardedFor = createHeaderValues(request, X_FORWARDED_FOR_HEADER);
        if (forwardedFor != null) {
            context.put(X_FORWARDED_FOR, forwardedFor);
        }

        if (request.getHeader(FORWARDED_HEADER) != null) {
            var result = HttpForwardedHeaderParser.parse(request.getHeaders(FORWARDED_HEADER));
            context.put(FORWARDED, createHeaderValues(request, FORWARDED_HEADER));

            context.put(FORWARDED_BY, String.join("|", result.getBy()));
            context.put(FORWARDED_FOR, String.join("|", result.getAFor()));
            context.put(FORWARDED_HOST, String.join("|", result.getHost()));
            context.put(FORWARDED_PROTO, String.join("|", result.getProto()));
            context.put(
                    FORWARDED_EXTENSIONS,
                    result.getExtensions().stream().map(ForwardedExtension::toString).collect(Collectors.joining("|"))
            );
        }
    }

    void addAdditionalHeaders(HttpServletRequest request) {

        for (TraceIdFilterConfig.AdditionalHeader additionalHeader : configuration.getAdditionalHeaders()) {
            String header = createHeaderValues(request, additionalHeader.getHeaderName());

            if (header != null) {
                context.put(additionalHeader.getContextKey(), header);
            } else if (log.isDebugEnabled()) {
                log.debug("Header with name '{}' not present in request", additionalHeader.getHeaderName());
            }
        }
    }

    void logAllHeaders(HttpServletRequest request) {
        if (!log.isDebugEnabled()) return;

        List<String> headers = new ArrayList<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value;

            if (configuration.getSensitiveHeaders().contains(key)) {
                value = "***";
            } else {
                value = createHeaderValues(request, key);
            }

            headers.add(key + "=[" + value + "]");
        }

        log.debug("Request headers: " + String.join(",", headers));
    }

    private String createHeaderValues(HttpServletRequest request, String key) {
        StringBuilder builder = new StringBuilder();
        Enumeration<String> headerValues = request.getHeaders(key);
        if (!headerValues.hasMoreElements()) return null;

        while (headerValues.hasMoreElements()) {
            if (builder.length() != 0) {
                builder.append("|");
            }

            builder.append(headerValues.nextElement());
        }

        return builder.toString();
    }

    public enum Feature {
        ADD_PATH,
        ADD_URL,
        ADD_METHOD,
        ADD_QUERY_STRING,
        ADD_FORWARDED_FOR_DATA,
        ADD_USER_AGENT
    }
}

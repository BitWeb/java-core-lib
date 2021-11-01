package ee.bitweb.core.request_id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import static ee.bitweb.core.request_id.RequestIdUtil.*;

@Slf4j
@Order(Integer.MIN_VALUE + 20)
public class RequestIdFilter implements Filter {

    public static final String PATH = "path";
    public static final String URL = "url";
    public static final String METHOD = "method";
    public static final String USER_AGENT = "user_agent";
    public static final String FORWARDED_FOR = "forwarded_for";
    public static final String FORWARDED_BY = "forwarded_by";
    public static final String FORWARDED_HOST = "forwarded_host";
    public static final String AMZN_TRACE_ID = "amzn_trace_id";
    private static final String AWS_API_GW_VIA_HEADER = "HTTP/1.1 AmazonAPIGateway";

    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            String userAgent = RequestIdUtil.getUserAgent(httpServletRequest);

            MDC.put(PATH, httpServletRequest.getServletPath());
            MDC.put(URL, getUrl(httpServletRequest));
            MDC.put(METHOD, httpServletRequest.getMethod());
            MDC.put(USER_AGENT, userAgent);

            addRequestId(httpServletRequest, userAgent);
            addServiceRequestId(httpServletRequest, userAgent);
            addIpAddresses(httpServletRequest, userAgent);
            addAmznTraceId(httpServletRequest, userAgent);

            if (response instanceof HttpServletResponse) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;

                RequestId.generateIfMissing();
                httpServletResponse.addHeader(RequestId.HTTP_HEADER, MDC.get(RequestId.MDC));
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private void addRequestId(HttpServletRequest httpServletRequest, String userAgent) {
        String requestId = httpServletRequest.getHeader(RequestId.HTTP_HEADER);
        if (!StringUtils.hasLength(requestId)) {
            RequestId.generateAndPut();

            if (AWS_API_GW_VIA_HEADER.equalsIgnoreCase(httpServletRequest.getHeader(HttpHeaders.VIA))) {
                log.info("Generated request ID for request from AWS API Gateway");
            } else if (isNotIgnored(userAgent)) {
                log.warn("Request ID not found in HTTP request, generated new");
                logHeaders(httpServletRequest);
            }
        } else {
            MDC.put(RequestId.MDC, requestId);
        }
    }

    private void addServiceRequestId(HttpServletRequest httpServletRequest, String userAgent) {
        String serviceRequestId = httpServletRequest.getHeader(ServiceRequestId.HTTP_HEADER);
        if (!StringUtils.hasLength(serviceRequestId)) {
            ServiceRequestId.initialize();

            if (AWS_API_GW_VIA_HEADER.equalsIgnoreCase(httpServletRequest.getHeader(HttpHeaders.VIA))) {
                log.info("Generated service request ID for request from AWS API Gateway");
            } else if (isNotIgnored(userAgent)) {
                log.warn("Service Request ID not found in HTTP request, generated new");
                logHeaders(httpServletRequest);
            }
        } else {
            ServiceRequestId.initialize(serviceRequestId);
        }
    }

    private void logHeaders(HttpServletRequest request) {
        if (!log.isDebugEnabled()) return;

        List<String> headers = new ArrayList<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);

            headers.add(key + "=[" + value + "]");
        }

        log.debug("Request headers: " + String.join(",", headers));
    }

    private String getUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString == null) return url;

        return url + "?" + queryString;
    }

    private void addIpAddresses(HttpServletRequest request, String userAgent) {
        String forwarded = request.getHeader(FORWARDED_HEADER);
        if (forwarded != null) {
            Matcher matcher = FORWARDED_PATTERN.matcher(forwarded);

            if (matcher.matches()) {
                MDC.put(FORWARDED_BY, matcher.group(1));
                MDC.put(FORWARDED_FOR, matcher.group(2));
                MDC.put(FORWARDED_HOST, matcher.group(3));

                return;
            }
        }

        String forwardedFor = request.getHeader(X_FORWARDED_FOR_HEADER);
        if (forwardedFor != null) {
            MDC.put(FORWARDED_FOR, forwardedFor);

            return;
        }

        if (isNotIgnored(userAgent)) {
            log.warn("Could not find IP address from headers");
            logHeaders(request);
        }
    }

    private void addAmznTraceId(HttpServletRequest request, String userAgent) {
        String header = request.getHeader("x-amzn-trace-id");
        if (header != null) {
            MDC.put(AMZN_TRACE_ID, header);

            return;
        }

        if (isNotIgnored(userAgent)) {
            log.warn("Could not add x-amzn-trace-id to MDC");
            logHeaders(request);
        }
    }
}

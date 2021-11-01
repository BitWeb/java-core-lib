package ee.bitweb.core.request_id;

import java.util.Map;

import ee.bitweb.core.util.StringUtil;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Small class to contain different names for request ID in different contexts.
 *
 * Simple usage:
 * MDC.put(RequestId.MDC, request.getHeader(RequestId.HTTP_HEADER))
 *
 * Implemented as a class instead of an enum to simplify usage. As most cases require string, using enum would require
 * to write the sample like this:
 * MDC.put(RequestId.MDC.toString(), request.getHeader(RequestId.HTTP_HEADER.toString()))
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestId {

    public static final String MDC = "request_id";
    public static final String HTTP_HEADER = "X-Request-ID";
    public static final String JMS_PROPERTY = "request-id";

    private static final int REQUEST_ID_LENGTH = 20;

    public static String generate() {
        return StringUtil.random(REQUEST_ID_LENGTH);
    }

    public static void generateAndPut() {
        org.slf4j.MDC.put(MDC, generate());
    }

    public static void generateIfMissing() {
        String id = org.slf4j.MDC.get(MDC);
        if (StringUtils.hasLength(id)) return;

        generateAndPut();

        log.warn("Request ID not found in MDC, generated new.");
    }

    public static String generateIfMissingAndGet() {
        generateIfMissing();

        return org.slf4j.MDC.get(MDC);
    }

    public static Map<String, String> getCopyOfContextMap() {
        RequestId.generateIfMissing();

        return org.slf4j.MDC.getCopyOfContextMap();
    }
}

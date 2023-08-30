package ee.bitweb.core.audit.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.audit.AuditLogProperties;
import io.micrometer.core.instrument.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class RequestHeadersMapper implements AuditLogDataMapper {

    private final AuditLogProperties properties;
    private final ObjectMapper mapper;

    public static final String KEY = "request_headers";

    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();

        for (String additionalHeader : properties.getRequestHeaders()) {
            String headerValue = request.getHeader(additionalHeader);

            if (StringUtils.isEmpty(headerValue)) continue;

            result.put(additionalHeader, createHeaderValues(request, additionalHeader));
        }

        try {
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            log.warn("There was an error while parsing request headers");
        }

        return null;
    }

    public String getKey() {
        return KEY;
    }

    private String createHeaderValues(HttpServletRequest request, String key) {
        StringBuilder builder = new StringBuilder();
        Enumeration<String> headerValues = request.getHeaders(key);
        if (!headerValues.hasMoreElements()) return null;

        while (headerValues.hasMoreElements()) {
            if (builder.length() != 0) {
                builder.append("|");
            }
            String headerValue = headerValues.nextElement();
            if (properties.getSensitiveHeaders().stream().anyMatch(a -> a.equalsIgnoreCase(key))) {
                headerValue = String.format("Len(%s)", headerValue.length());
            }

            builder.append(headerValue);
        }

        return builder.toString();
    }
}

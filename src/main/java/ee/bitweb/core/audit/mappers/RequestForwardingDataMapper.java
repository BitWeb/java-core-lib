package ee.bitweb.core.audit.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.audit.AuditLogProperties;
import ee.bitweb.core.util.HttpForwardedHeaderParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class RequestForwardingDataMapper extends AbstractAuditLogDataMapper {

    public static final String KEY = "forwarded";

    public static final String FORWARDED_BY = "forwarded_by";
    public static final String FORWARDED_FOR = "forwarded_for";
    public static final String FORWARDED_HOST = "forwarded_host";
    public static final String FORWARDED_PROTO = "forwarded_proto";
    public static final String FORWARDED_EXTENSIONS = "forwarded_extensions";
    public static final String X_FORWARDED_FOR = "x_forwarded_for";
    private static final String FORWARDED_HEADER = "Forwarded";
    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final AuditLogProperties properties;
    private final ObjectMapper mapper;

    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> value = new HashMap<>();

        String forwardedFor = createHeaderValues(request, X_FORWARDED_FOR_HEADER);
        if (forwardedFor != null) {
            value.put(X_FORWARDED_FOR, forwardedFor);
        }

        if (request.getHeader(FORWARDED_HEADER) != null) {
            value.put(KEY, createHeaderValues(request, FORWARDED_HEADER));
            if (!isSensitive(FORWARDED_HEADER)) {
                parseAndAddForwardedMetadata(request, value);
            }
        }

        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("There was an error while parsing request headers");
        }

        return null;
    }

    void parseAndAddForwardedMetadata(HttpServletRequest request, Map<String, String> data) {
        var result = HttpForwardedHeaderParser.parse(request.getHeaders(FORWARDED_HEADER));
        data.put(FORWARDED_BY, String.join("|", result.getBy()));
        data.put(FORWARDED_FOR, String.join("|", result.getAFor()));
        data.put(FORWARDED_HOST, String.join("|", result.getHost()));
        data.put(FORWARDED_PROTO, String.join("|", result.getProto()));
        data.put(
                FORWARDED_EXTENSIONS,
                result.getExtensions().stream().map(
                        HttpForwardedHeaderParser.ForwardedExtension::toString
                ).collect(Collectors.joining("|"))
        );
    }

    @Override
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
            if (isSensitive(key)) {
                headerValue = String.format("Len(%s)", headerValue.length());
            }

            builder.append(headerValue);
        }

        return builder.toString();
    }

    private boolean isSensitive(String header) {
        return properties.getSensitiveHeaders().stream().anyMatch(a -> a.equalsIgnoreCase(header));
    }
}

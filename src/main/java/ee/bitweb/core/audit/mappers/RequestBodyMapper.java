package ee.bitweb.core.audit.mappers;

import ee.bitweb.core.audit.AuditLogProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

@RequiredArgsConstructor
public class RequestBodyMapper implements AuditLogDataMapper{

    public static final String KEY = "request_body";

    private final AuditLogProperties properties;

    @Override
    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            String content = new String(wrapper.getContentAsByteArray());


            if (content.length() > properties.getMaxLoggableRequestSize()) {
                content = String.format("%s ... Content size: %s characters", content.substring(0, (int) properties.getMaxLoggableRequestSize()),  content.length());
            }

            if (StringUtils.hasText(content)) {
                return content;
            }
        }

        return null;
    }

    @Override
    public String getKey() {

        return KEY;
    }
}

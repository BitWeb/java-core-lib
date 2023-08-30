package ee.bitweb.core.audit.mappers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

public class RequestBodyMapper implements AuditLogDataMapper{

    public static final String KEY = "request_body";

    @Override
    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        if (request instanceof ContentCachingRequestWrapper wrapper) {
            String content = new String(wrapper.getContentAsByteArray());

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

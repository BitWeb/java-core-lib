package ee.bitweb.core.audit.mappers;

import ee.bitweb.core.exception.CoreException;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface AuditLogDataMapper {

    String getValue(HttpServletRequest request, HttpServletResponse response);
    String getKey();

    default void map(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, Map<String, String> container) {
        if (container.containsKey(getKey())) {
            throw new CoreException(String.format("Audit log container already contains value for key %s", getKey()));
        }

        container.put(getKey(), getValue(request, response));
    }
}

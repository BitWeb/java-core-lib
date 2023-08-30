package ee.bitweb.core.audit.mappers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequestMethodMapper implements AuditLogDataMapper {

    public static final String KEY = "method";

    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        return request.getMethod();
    }

    public String getKey() {
        return KEY;
    }
}

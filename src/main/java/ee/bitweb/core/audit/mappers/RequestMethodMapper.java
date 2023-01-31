package ee.bitweb.core.audit.mappers;

import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

package ee.bitweb.core.audit.mappers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ResponseStatusMapper implements AuditLogDataMapper {

    public static final String KEY = "response_status";

    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        return String.valueOf(response.getStatus());
    }

    public String getKey() {
        return KEY;
    }
}

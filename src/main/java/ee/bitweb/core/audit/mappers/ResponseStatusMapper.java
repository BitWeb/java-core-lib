package ee.bitweb.core.audit.mappers;

import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class ResponseStatusMapper implements AuditLogDataMapper {

    public static final String KEY = "response_status";

    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        return String.valueOf(response.getStatus());
    }

    public String getKey() {
        return KEY;
    }
}

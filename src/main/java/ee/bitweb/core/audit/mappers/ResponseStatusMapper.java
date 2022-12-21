package ee.bitweb.core.audit.mappers;

import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class ResponseStatusMapper extends AbstractAuditLogDataMapper {

    public static final String KEY = "response_status";

    @Override
    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        return String.valueOf(response.getStatus());
    }

    @Override
    public String getKey() {
        return KEY;
    }
}

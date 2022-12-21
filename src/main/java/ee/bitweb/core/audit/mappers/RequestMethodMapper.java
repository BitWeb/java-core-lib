package ee.bitweb.core.audit.mappers;

import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class RequestMethodMapper extends AbstractAuditLogDataMapper {

    public static final String KEY = "method";

    @Override
    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        return request.getMethod();
    }

    @Override
    public String getKey() {
        return KEY;
    }
}

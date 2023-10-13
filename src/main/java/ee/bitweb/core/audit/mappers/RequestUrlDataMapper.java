package ee.bitweb.core.audit.mappers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RequestUrlDataMapper implements AuditLogDataMapper {

    public static final String KEY = "url";

    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();

        if (queryString != null) {
            url = url + "?" + queryString ;
        }

        return url;
    }

    public String getKey() {
        return KEY;
    }
}

package ee.bitweb.core.audit.mappers;

import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class RequestUrlDataMapper extends AbstractAuditLogDataMapper {

    public static final String KEY = "url";

    @Override
    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();

        if (queryString != null) {
            url = url + "?" + queryString ;
        }

        return url;
    }

    @Override
    public String getKey() {
        return KEY;
    }
}

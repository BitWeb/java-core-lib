package ee.bitweb.core.audit.testcomponent;

import ee.bitweb.core.audit.mappers.AuditLogDataMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Profile("CustomAuditLogMapper")
public class CustomAuditLogMapper implements AuditLogDataMapper {

    public static String KEY = "RANDOM_KEY";

    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        return "SOME_RANDOM_VALUE";
    }

    public String getKey() {
        return KEY;
    }
}

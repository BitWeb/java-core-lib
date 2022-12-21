package ee.bitweb.core.audit.testcomponent;

import ee.bitweb.core.audit.mappers.AbstractAuditLogDataMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Profile("CustomAuditLogMapper")
public class CustomAuditLogMapper extends AbstractAuditLogDataMapper {

    public static String KEY = "RANDOM_KEY";

    @Override
    public String getValue(HttpServletRequest request, HttpServletResponse response) {
        return "SOME_RANDOM_VALUE";
    }

    @Override
    public String getKey() {
        return KEY;
    }
}

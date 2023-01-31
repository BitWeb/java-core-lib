package ee.bitweb.core.audit.writers;

import ee.bitweb.core.audit.AuditLogFilter;
import ee.bitweb.core.audit.mappers.RequestMethodMapper;
import ee.bitweb.core.audit.mappers.RequestUrlDataMapper;
import ee.bitweb.core.audit.mappers.ResponseStatusMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.util.Map;

@Slf4j
@ConditionalOnExpression(
        "${ee.bitweb.core.audit.autoconfiguration:false}"
)
public class AuditLogLoggerWriterAdapter implements AuditLogWriteAdapter {

    public static final String AUDIT = "audit";

    @Override
    public void write(Map<String, String> container) {
        Map<String, String> currentContext = MDC.getCopyOfContextMap();
        container.put(AUDIT, "1");
        MDC.setContextMap(container);
        log.info(
                "{} {} {} in {} ms",
                get(container, RequestMethodMapper.KEY),
                get(container, RequestUrlDataMapper.KEY),
                get(container, ResponseStatusMapper.KEY),
                get(container, AuditLogFilter.DURATION_KEY));
        MDC.setContextMap(currentContext);
    }

    private String get(Map<String, String> container, String key) {
        if (container.containsKey(key)) {
            return container.get(key);
        }

        return "UNKNOWN";
    }
}

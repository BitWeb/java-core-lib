package ee.bitweb.core.audit.testcomponent;

import ee.bitweb.core.audit.writers.AuditLogWriteAdapter;
import lombok.Getter;

import java.util.Map;

@Getter
public class CustomAuditLogWriter implements AuditLogWriteAdapter {

    private Map<String, String> result;

    @Override
    public void write(Map<String, String> container) {
        result = container;
    }

    public void reset() {
        result = null;
    }
}

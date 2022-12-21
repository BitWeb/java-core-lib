package ee.bitweb.core.audit.writers;

import java.util.Map;

public interface AuditLogWriteAdapter {

    void write(Map<String, String> container);
}

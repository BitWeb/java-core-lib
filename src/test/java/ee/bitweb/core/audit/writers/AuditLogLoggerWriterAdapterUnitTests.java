package ee.bitweb.core.audit.writers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ee.bitweb.core.utils.MemoryAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AuditLogLoggerWriterAdapterUnitTests {

    private Logger logger;
    private MemoryAppender memoryAppender;
    private AuditLogLoggerWriterAdapter adapter;

    @BeforeEach
    void beforeEach() {
        adapter = new AuditLogLoggerWriterAdapter();
        logger = (Logger) LoggerFactory.getLogger(AuditLogLoggerWriterAdapter.LOGGER_NAME);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.INFO);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void createASingleLogWithNoSecondaryFields() {

        adapter.write(createDefaultMap());

        List<ILoggingEvent> events = memoryAppender.getLoggedEvents();

        assertEquals(1, events.size());
        assertEquals(
                "Method(POST),  URL(/some-url) Status(200 OK) ResponseSize(9) Duration(123 ms)",
                events.get(0).getFormattedMessage()
        );
        assertEquals(8, events.get(0).getMDCPropertyMap().size());
        assertNull(MDC.getCopyOfContextMap());
    }

    @Test
    void createASingleLogWithSecondaryFieldsAndDebugDisabled() {
        MDC.put("key", "value");
        adapter.write(createDefaultMap());

        List<ILoggingEvent> events = memoryAppender.getLoggedEvents();

        assertEquals(1, events.size());
        assertEquals(
                "Method(POST),  URL(/some-url) Status(200 OK) ResponseSize(9) Duration(123 ms)",
                events.get(0).getFormattedMessage()
        );
        assertEquals(8, events.get(0).getMDCPropertyMap().size());
        assertEquals(1, MDC.getCopyOfContextMap().size());
        assertEquals("value", MDC.get("key"));
    }

    @Test
    void createASecondaryLogWithSecondaryFieldsAndDebugEnabled() {
        logger.setLevel(Level.DEBUG);
        adapter.write(createDefaultMap());

        List<ILoggingEvent> events = memoryAppender.getLoggedEvents();

        assertEquals(2, events.size());
        assertEquals(
                "Method(POST),  URL(/some-url) Status(200 OK) ResponseSize(9) Duration(123 ms)",
                events.get(0).getFormattedMessage()
        );
        assertEquals(
                "Debug audit log",
                events.get(1).getFormattedMessage()
        );
        assertEquals(8, events.get(0).getMDCPropertyMap().size());
        assertEquals(4, events.get(1).getMDCPropertyMap().size());
        assertNull(MDC.getCopyOfContextMap());
    }


    public Map<String, String> createDefaultMap() {
        Map<String, String> map = new HashMap<>();

        map.put("trace_id", "some-trace-id");
        map.put("response_status", HttpStatus.OK.toString());
        map.put("url", "/some-url");
        map.put("duration", "123");
        map.put("method", "POST");
        map.put("request_body", "Some payload");
        map.put("response_body", "Some body");

        return map;
    }
}

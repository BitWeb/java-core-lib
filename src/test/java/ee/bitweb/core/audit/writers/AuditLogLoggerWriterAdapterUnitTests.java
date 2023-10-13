package ee.bitweb.core.audit.writers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ee.bitweb.core.audit.AuditLogFilter;
import ee.bitweb.core.audit.mappers.*;
import ee.bitweb.core.utils.MemoryAppender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    void createASingleLogWithNoSecondaryFields() {

        adapter.write(createDefaultMap());

        List<ILoggingEvent> events = memoryAppender.getLoggedEvents();

        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(
                "Method(POST),  URL(/some-url) Status(200 OK) ResponseSize(9) Duration(123 ms)",
                events.get(0).getFormattedMessage()
        );
        Assertions.assertEquals(8, events.get(0).getMDCPropertyMap().size());
    }

    @Test
    void createASingleLogWithSecondaryFieldsAndDebugDisabled() {
        adapter.write(createDefaultMap());

        List<ILoggingEvent> events = memoryAppender.getLoggedEvents();

        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals(
                "Method(POST),  URL(/some-url) Status(200 OK) ResponseSize(9) Duration(123 ms)",
                events.get(0).getFormattedMessage()
        );
        Assertions.assertEquals(8, events.get(0).getMDCPropertyMap().size());
    }

    @Test
    void createASecondaryLogWithSecondaryFieldsAndDebugEnabled() {
        logger.setLevel(Level.DEBUG);
        adapter.write(createDefaultMap());

        List<ILoggingEvent> events = memoryAppender.getLoggedEvents();

        Assertions.assertEquals(2, events.size());
        Assertions.assertEquals(
                "Method(POST),  URL(/some-url) Status(200 OK) ResponseSize(9) Duration(123 ms)",
                events.get(0).getFormattedMessage()
        );
        Assertions.assertEquals(
                "Debug audit log",
                events.get(1).getFormattedMessage()
        );
        Assertions.assertEquals(8, events.get(0).getMDCPropertyMap().size());
        Assertions.assertEquals(4, events.get(1).getMDCPropertyMap().size());

    }


    public Map<String, String> createDefaultMap() {
        Map<String, String> map = new HashMap<>();

        map.put(TraceIdMapper.KEY, "some-trace-id");
        map.put(ResponseStatusMapper.KEY, HttpStatus.OK.toString());
        map.put(RequestUrlDataMapper.KEY, "/some-url");
        map.put(AuditLogFilter.DURATION_KEY, "123");
        map.put(RequestMethodMapper.KEY, "POST");
        map.put(RequestBodyMapper.KEY, "Some payload");
        map.put(ResponseBodyMapper.KEY, "Some body");

        return map;
    }
}

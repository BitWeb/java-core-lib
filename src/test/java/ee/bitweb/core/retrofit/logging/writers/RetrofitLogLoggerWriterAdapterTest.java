package ee.bitweb.core.retrofit.logging.writers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ee.bitweb.core.exception.CoreException;
import ee.bitweb.core.utils.MemoryAppender;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class RetrofitLogLoggerWriterAdapterTest {

    Logger logger;
    MemoryAppender memoryAppender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger("RetrofitLogger");
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
    @DisplayName("Event is logged with correct MDC and MDC is restored")
    void testMessageIsWrittenToLoggerWithCorrectContext() {
        // given
        MDC.put("current", "1");
        Map<String, String> container = Map.of(
                "request_method", "GET",
                "request_url", "https://localhost:3000/api?data=true&test",
                "response_code", "404",
                "duration", "14"
        );
        RetrofitLogLoggerWriterAdapter writer = new RetrofitLogLoggerWriterAdapter();

        // when
        writer.write(container);

        // then validate logging event
        assertEquals(1, memoryAppender.getSize());
        ILoggingEvent loggingEvent = memoryAppender.getLoggedEvents().get(0);
        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertEquals("GET https://localhost:3000/api?data=true&test 404 -bytes 14ms", loggingEvent.getFormattedMessage());

        // then validate logging event MDC
        assertAll(
                () -> assertEquals(5, loggingEvent.getMDCPropertyMap().size()),
                () -> assertEquals("1", loggingEvent.getMDCPropertyMap().get("current")),
                () -> assertEquals("404", loggingEvent.getMDCPropertyMap().get("response_code")),
                () -> assertEquals("GET", loggingEvent.getMDCPropertyMap().get("request_method")),
                () -> assertEquals("https://localhost:3000/api?data=true&test", loggingEvent.getMDCPropertyMap().get("request_url")),
                () -> assertEquals("14", loggingEvent.getMDCPropertyMap().get("duration")),
                () -> assertNull(loggingEvent.getMDCPropertyMap().get("response_body_size"))
        );

        // then validate current MDC
        assertAll(
                () -> assertEquals(1, MDC.getCopyOfContextMap().size()),
                () -> assertEquals("1", MDC.getCopyOfContextMap().get("current"))
        );
    }

    @Test
    @DisplayName("Event is logged with correct MDC and an empty MDC is restored")
    void validateMdcIsRestoredWhenEmpty() {
        // given
        Map<String, String> container = Map.of(
                "RequestMethod", "GET",
                "RequestUrl", "https://localhost:3000/api?data=true&test",
                "ResponseCode", "404",
                "ResponseBodySize", "0",
                "Duration", "14"
        );
        RetrofitLogLoggerWriterAdapter writer = new RetrofitLogLoggerWriterAdapter();

        // when
        writer.write(container);

        // then validate current MDC
        assertAll(
                () -> assertNull(MDC.getCopyOfContextMap())
        );
    }

    @Test
    @DisplayName("Error should be logged when logging level less than INFO")
    void validateErrorIsLoggedWhenLoggingLevelIsLessThanInfo() {
        // given
        Map<String, String> container = Map.of(
                "RequestMethod", "GET",
                "RequestUrl", "https://localhost:3000/api?data=true&test",
                "ResponseCode", "404",
                "ResponseBodySize", "0",
                "Duration", "14"
        );
        RetrofitLogLoggerWriterAdapter writer = new RetrofitLogLoggerWriterAdapter();
        logger.setLevel(Level.WARN);

        // when
        writer.write(container);

        // then
        assertEquals(1, memoryAppender.getSize());
        ILoggingEvent loggingEvent = memoryAppender.getLoggedEvents().get(0);
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertEquals(
                "Retrofit interceptor has been enabled, but RetrofitLogLoggerWriterAdapter cannot write as log level does not permit INFO entries. " +
                        "This behaviour is strongly discouraged as the interceptor consumes resources for no real result. Please set property " +
                        "ee.bitweb.core.retrofit.logging-level=NONE if you wish to avoid this logging.",
                loggingEvent.getFormattedMessage()
        );
    }

    @Test
    void validateExceptionIsThrownWhenLoggingLevelIsLessThanError() {
        // given
        Map<String, String> container = Map.of(
                "RequestMethod", "GET",
                "RequestUrl", "https://localhost:3000/api?data=true&test",
                "ResponseCode", "404",
                "ResponseBodySize", "0",
                "Duration", "14"
        );
        RetrofitLogLoggerWriterAdapter writer = new RetrofitLogLoggerWriterAdapter();
        logger.setLevel(Level.OFF);

        // when
        CoreException ex = assertThrows(CoreException.class, () -> writer.write(container));

        // then
        assertEquals(
                "Retrofit interceptor has been enabled, but RetrofitLogLoggerWriterAdapter cannot write as log level does not permit INFO entries. " +
                        "This behaviour is strongly discouraged as the interceptor consumes resources for no real result. Please set property " +
                        "ee.bitweb.core.retrofit.logging-level=NONE if you wish to avoid this logging.",
                ex.getMessage()
        );
    }
}

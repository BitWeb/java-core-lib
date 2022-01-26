package ee.bitweb.core.trace;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;

import ee.bitweb.core.utils.MemoryAppender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TraceIdFilterTest {

    @Mock
    FilterChain chain;

    Logger logger;
    MemoryAppender memoryAppender;

    @BeforeEach
    void beforeEach() {
        MDC.clear();
        logger = (Logger) LoggerFactory.getLogger(TraceIdFilter.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    @DisplayName("logAllHeaders() should log header with one value")
    void logHeadersLogsOneHeaderWithOneValue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "123test");

        new TraceIdFilter().logAllHeaders(request);

        assertEquals(1, memoryAppender.search("Request headers: test=[123test]", Level.DEBUG).size());
        assertEquals(1, memoryAppender.getSize());

    }

    @Test
    @DisplayName("logAllHeaders() should log headers with multiple values in DEBUG level")
    void logHeadersLogsHeadersWithMultipleValues() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "test123");
        request.addHeader("test", "123test");
        request.addHeader("x-test", "some value");

        new TraceIdFilter().logAllHeaders(request);

        assertEquals(1, memoryAppender.search(
                "Request headers: test=[test123|123test],x-test=[some value]",
                Level.DEBUG).size()
        );
        assertEquals(1, memoryAppender.getSize());
    }

    @Test
    @DisplayName("logAllHeaders() should not log headers with multiple values in INFO level")
    void onLoggingMultipleValueHeadersWithInfoLevelNoLogsShouldBeCreated() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "test123");
        request.addHeader("test", "123test");
        request.addHeader("x-test", "some value");

        logger.setLevel(Level.INFO);

        new TraceIdFilter().logAllHeaders(request);

        assertEquals(0, memoryAppender.getSize());
    }

    @Test
    @DisplayName("logAllHeaders() should log mask sensitive header values in DEBUG level")
    void logHeadersMasksSensitiveHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("authorization", "123test");

        new TraceIdFilter().logAllHeaders(request);

        assertEquals(1, memoryAppender.search(
                "Request headers: authorization=[***]",
                Level.DEBUG).size()
        );
    }

    @Test
    @DisplayName("logAllHeaders() should not log mask sensitive header values in INFO level")
    void infoLevelLoggerShouldNotLogMaskedSensitiveHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("authorization", "123test");

        logger.setLevel(Level.INFO);

        new TraceIdFilter().logAllHeaders(request);

        assertEquals(0, memoryAppender.getSize());
    }

    @Test
    @DisplayName("addAdditionalHeaders() when using default Customizer, no headers should be added to MDC")
    void testNoAdditionalHeadersAreAddedToMdc() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "123test");

        new TraceIdFilter().addAdditionalHeaders(request);

        assertEquals(0, memoryAppender.getSize());
        assertNull(MDC.getCopyOfContextMap());
    }

    @Test
    @DisplayName("addAdditionalHeaders() when using custom Customizer, selected headers should be added to MDC")
    void testAdditionalHeadersWithSingleValueAreAddedToMdc() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "123test");
        request.addHeader("x-test", "321test");

        TraceIdCustomizer customizer = TraceIdCustomizerImpl.builder().additionalHeader("test").build();
        new TraceIdFilter(customizer).addAdditionalHeaders(request);

        assertEquals("123test", MDC.get("test"));
        assertEquals(1, MDC.getCopyOfContextMap().size());
    }

    @Test
    @DisplayName("addAdditionalHeaders() when using custom Customizer, selected header with multiple values should be added to MDC")
    void testAdditionalHeadersWithMultipleValuesAreAddedToMdc() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "123test");
        request.addHeader("test", "test123");

        TraceIdCustomizer customizer = TraceIdCustomizerImpl.builder().additionalHeader("test").build();
        new TraceIdFilter(customizer).addAdditionalHeaders(request);

        assertEquals("123test|test123", MDC.get("test"));
        assertEquals(1, MDC.getCopyOfContextMap().size());
        assertEquals(0, memoryAppender.getSize());
    }

    @Test
    @DisplayName("addAdditionalHeaders() when using custom Customizer, logs debug message when header is not available")
    void testAdditionalHeadersLogsWhenHeaderIsNotFoundInRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");

        TraceIdCustomizer customizer = TraceIdCustomizerImpl.builder().additionalHeader("test").build();
        new TraceIdFilter(customizer).addAdditionalHeaders(request);

        assertEquals(1, memoryAppender.search(
                "Header with name 'test' not present in request",
                Level.DEBUG).size()
        );
        assertEquals(1, memoryAppender.getSize());
        assertNull(MDC.getCopyOfContextMap());
    }

    @Test
    @DisplayName("addForwardingInfo() copies multiple IP addresses from X-Forwarded-For header to MDC")
    void testAddIpAddressesUsesXForwardedForHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("x-forwarded-for", "192.168.69.145,192.168.69.1");

        new TraceIdFilter().addForwardingInfo(request);

        assertEquals("192.168.69.145,192.168.69.1", MDC.get("x_forwarded_for"));
        assertEquals(1, MDC.getCopyOfContextMap().size());
        assertEquals(0, memoryAppender.getSize());
    }

    @Test
    @DisplayName("addForwardingInfo() copies multiple IP addresses from X-Forwarded-For headers to MDC")
    void testAddIpAddressesUsesXForwardedForHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("x-forwarded-for", "192.168.69.145");
        request.addHeader("x-forwarded-for", "192.168.69.1");

        new TraceIdFilter().addForwardingInfo(request);

        assertEquals("192.168.69.145|192.168.69.1", MDC.get("x_forwarded_for"));
        assertEquals(1, MDC.getCopyOfContextMap().size());
        assertEquals(0, memoryAppender.getSize());
    }

    @Test
    @DisplayName("addForwardingInfo() parses Forwarded header and adds info to MDC")
    void testAddForwardingHeadersParsesForwardedHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("forwarded", "for=192.0.2.43,for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com;secret=ruewiu");

        new TraceIdFilter().addForwardingInfo(request);

        assertEquals("for=192.0.2.43,for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com;secret=ruewiu", MDC.get("forwarded"));
        assertEquals("203.0.113.60", MDC.get("forwarded_by"));
        assertEquals("192.0.2.43|198.51.100.17", MDC.get("forwarded_for"));
        assertEquals("example.com", MDC.get("forwarded_host"));
        assertEquals("http", MDC.get("forwarded_proto"));
        assertEquals("secret=ruewiu", MDC.get("forwarded_extensions"));
        assertEquals(6, MDC.getCopyOfContextMap().size());
        assertEquals(0, memoryAppender.getSize());
    }

    @Test
    @DisplayName("getUrl() returns path")
    void testGetUrlReturnsPath() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");

        assertEquals("http://localhost/this", new TraceIdFilter().getUrl(request));
    }

    @Test
    @DisplayName("getUrl() returns path and query string")
    void testGetUrlReturnsPathAndQueryString() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.setQueryString("size=100");

        assertEquals("http://localhost/this?size=100", new TraceIdFilter().getUrl(request));
    }

    @Test
    @DisplayName("getUserAgent() returns user agent")
    void testGetUserAgentReturnsUserAgent() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("User-Agent", "some-user-agent");

        assertEquals("some-user-agent", new TraceIdFilter().getUserAgent(request));
    }

    @Test
    @DisplayName("getUserAgent() returns user agent missing")
    void testGetUserAgentReturnsUserAgentMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");

        assertEquals("MISSING", new TraceIdFilter().getUserAgent(request));
    }

    @Test
    @DisplayName("doFilter() does everything needed")
    void testDoFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.setServletPath("/this");
        MockHttpServletResponse response = new MockHttpServletResponse();
        logger.setLevel(Level.INFO);

        try (MockedStatic<MDC> mdcMock = Mockito.mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("trace_id")).thenReturn("generated_mock");

            new TraceIdFilter(new MockTraceIdProvider()).doFilter(request, response, chain);

            assertEquals("generated_mock", response.getHeader("X-Trace-ID"));
            mdcMock.verify(() -> MDC.put("trace_id", "generated_mock"));
            mdcMock.verify(() -> MDC.put("path", "/this"));
            mdcMock.verify(() -> MDC.put("url", "http://localhost/this"));
            mdcMock.verify(() -> MDC.put("method", "GET"));

            mdcMock.verify(() -> MDC.put("query_string", null));
            mdcMock.verify(() -> MDC.put("user_agent", "MISSING"));
            mdcMock.verify(() -> MDC.get("trace_id"));

            mdcMock.verify(MDC::clear);
            mdcMock.verifyNoMoreInteractions();

            verify(chain).doFilter(request, response);
            assertEquals(0, memoryAppender.getSize());
        }
    }
    static class MockTraceIdProvider implements TraceIdProvider {

        @Override
        public String generate(HttpServletRequest request) {
            return "generated_mock";
        }
    }
}

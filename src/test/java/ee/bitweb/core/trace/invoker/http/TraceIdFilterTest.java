package ee.bitweb.core.trace.invoker.http;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ee.bitweb.core.trace.context.MDCTraceIdContext;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import ee.bitweb.core.utils.MemoryAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

        createFilter().logAllHeaders(request);

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

        createFilter().logAllHeaders(request);

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

        createFilter().logAllHeaders(request);

        assertEquals(0, memoryAppender.getSize());
    }

    @Test
    @DisplayName("logAllHeaders() should log mask sensitive header values in DEBUG level")
    void logHeadersMasksSensitiveHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("authorization", "123test");

        createFilter().logAllHeaders(request);

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

        createFilter().logAllHeaders(request);

        assertEquals(0, memoryAppender.getSize());
    }

    @Test
    @DisplayName("addAdditionalHeaders() when using default Customizer, no headers should be added to MDC")
    void testNoAdditionalHeadersAreAddedToMdc() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "123test");

        createFilter().addAdditionalHeaders(request);

        assertEquals(0, memoryAppender.getSize());
        assertNull(MDC.getCopyOfContextMap());
    }

    @Test
    @DisplayName("addAdditionalHeaders() when using custom Customizer, selected headers should be added to MDC")
    void testAdditionalHeadersWithSingleValueAreAddedToMdc() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "123test");
        request.addHeader("x-test", "321test");

        TraceIdFilterConfig config = new TraceIdFilterConfig();
        config.getAdditionalHeaders().add(new TraceIdFilterConfig.AdditionalHeader("test"));
        createFilter(config).addAdditionalHeaders(request);

        assertEquals("123test", MDC.get("test"));
        assertEquals(1, MDC.getCopyOfContextMap().size());
    }

    @Test
    @DisplayName("addAdditionalHeaders() when using custom Customizer, selected header with multiple values should be added to MDC")
    void testAdditionalHeadersWithMultipleValuesAreAddedToMdc() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "123test");
        request.addHeader("test", "test123");

        TraceIdFilterConfig config = new TraceIdFilterConfig();
        config.getAdditionalHeaders().add(new TraceIdFilterConfig.AdditionalHeader("test"));

        createFilter(config).addAdditionalHeaders(request);

        assertEquals("123test|test123", MDC.get("test"));
        assertEquals(1, MDC.getCopyOfContextMap().size());
        assertEquals(0, memoryAppender.getSize());
    }

    @Test
    @DisplayName("addAdditionalHeaders() when using custom Customizer, logs debug message when header is not available")
    void testAdditionalHeadersLogsWhenHeaderIsNotFoundInRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");

        TraceIdFilterConfig config = new TraceIdFilterConfig();
        config.getAdditionalHeaders().add(new TraceIdFilterConfig.AdditionalHeader("test"));

        createFilter(config).addAdditionalHeaders(request);

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

        createFilter().addForwardingInfoIfEnabled(request);

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

        createFilter().addForwardingInfoIfEnabled(request);

        assertEquals("192.168.69.145|192.168.69.1", MDC.get("x_forwarded_for"));
        assertEquals(1, MDC.getCopyOfContextMap().size());
        assertEquals(0, memoryAppender.getSize());
    }

    @Test
    @DisplayName("addForwardingInfo() parses Forwarded header and adds info to MDC")
    void testAddForwardingHeadersParsesForwardedHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("forwarded", "for=192.0.2.43,for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com;secret=ruewiu");

        createFilter().addForwardingInfoIfEnabled(request);

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

        assertEquals("http://localhost/this", createFilter().getUrl(request));
    }

    @Test
    @DisplayName("getUrl() returns path and query string")
    void testGetUrlReturnsPathAndQueryString() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.setQueryString("size=100");

        assertEquals("http://localhost/this?size=100", createFilter().getUrl(request));
    }

    @Test
    @DisplayName("getUserAgent() returns user agent")
    void testGetUserAgentReturnsUserAgent() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("User-Agent", "some-user-agent");

        assertEquals("some-user-agent", createFilter().getUserAgent(request));
    }

    @Test
    @DisplayName("getUserAgent() returns user agent missing")
    void testGetUserAgentReturnsUserAgentMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");

        assertEquals("MISSING", createFilter().getUserAgent(request));
    }

    @Test
    @DisplayName("doFilter() does everything needed")
    void testDoFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.setServletPath("/this");
        request.addHeader("User-Agent", "some-user-agent");

        MockHttpServletResponse response = new MockHttpServletResponse();
        logger.setLevel(Level.INFO);

        try (MockedStatic<MDC> mdcMock = Mockito.mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("trace_id")).thenReturn("generated_mock");

            createFilter().doFilter(request, response, chain);

            assertEquals("generated_mock", response.getHeader("X-Trace-ID"));
            mdcMock.verify(() -> MDC.put("trace_id", "generated_mock"));
            mdcMock.verify(() -> MDC.put("path", "/this"));
            mdcMock.verify(() -> MDC.put("url", "http://localhost/this"));
            mdcMock.verify(() -> MDC.put("method", "GET"));

            mdcMock.verify(() -> MDC.put("query_string", null));
            mdcMock.verify(() -> MDC.put("user_agent", "some-user-agent"));
            mdcMock.verify(() -> MDC.get("trace_id"), times(2));

            mdcMock.verify(() -> MDC.remove(any()), times(2));
            mdcMock.verifyNoMoreInteractions();

            verify(chain).doFilter(request, response);
            assertEquals(0, memoryAppender.getSize());
        }
    }

    @Test
    @DisplayName("all disabled features are disabled")
    void testForDisabledFeatures() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.setServletPath("/this");
        request.addHeader("User-Agent", "some-user-agent");

        MockHttpServletResponse response = new MockHttpServletResponse();
        logger.setLevel(Level.INFO);

        try (MockedStatic<MDC> mdcMock = Mockito.mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("trace_id")).thenReturn("generated_mock");

            TraceIdFilterConfig config = new TraceIdFilterConfig();
            config.setEnabledFeatures(Collections.EMPTY_LIST);

            createFilter(config).doFilter(request, response, chain);

            assertEquals("generated_mock", response.getHeader("X-Trace-ID"));
            mdcMock.verify(() -> MDC.put("trace_id", "generated_mock"));
            mdcMock.verify(() -> MDC.put("path", "/this"), times(0));
            mdcMock.verify(() -> MDC.put("url", "http://localhost/this"), times(0));
            mdcMock.verify(() -> MDC.put("method", "GET"), times(0));
            mdcMock.verify(() -> MDC.put("query_string", null), times(0));
            mdcMock.verify(() -> MDC.put("user_agent", "some-user-agent"), times(0));
            mdcMock.verify(() -> MDC.get("trace_id"), times(2));

            mdcMock.verify(() -> MDC.remove(any()), times(2));
            mdcMock.verifyNoMoreInteractions();

            verify(chain).doFilter(request, response);
            assertEquals(0, memoryAppender.getSize());
        }
    }

    private TraceIdFilter createFilter() {
        return createFilter(new TraceIdFilterConfig());
    }

    private TraceIdFilter createFilter(TraceIdFilterConfig config) {
        MDCTraceIdContext context = MDCTraceIdContext.standard();

        return new TraceIdFilter(
                config,
                context,
                new HttpServletRequestTraceIdResolver(
                        config.getHeaderName(),
                        context,
                        new MockedCreator()
                )
        );
    }

    static class MockedCreator implements TraceIdCreator {
        public String generate(String traceId) {
            return "generated_mock";
        }
    }
}

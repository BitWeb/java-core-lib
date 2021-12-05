package ee.bitweb.core.trace;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.simplify4u.slf4jmock.LoggerMock;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TraceIdFilterTest {

    Logger logger;

    @Mock
    MDCAdapter mdcMock;

    @Mock
    FilterChain chain;

    @BeforeEach
    void setUp() {
        logger = mock(Logger.class);
        LoggerMock.setMock(TraceIdFilter.class, logger);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        LoggerMock.clearMock(TraceIdFilter.class);
    }

    @Test
    @DisplayName("logAllHeaders() should log header with one value")
    void logHeadersLogsOneHeaderWithOneValue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "123test");

        when(logger.isDebugEnabled()).thenReturn(true);

        new TraceIdFilter().logAllHeaders(request);

        verify(logger).debug("Request headers: test=[123test]");
        verifyNoMoreInteractions(logger);
    }

    @Test
    @DisplayName("logAllHeaders() should log headers with multiple values")
    void logHeadersLogsHeadersWithMultipleValues() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "test123");
        request.addHeader("test", "123test");
        request.addHeader("x-test", "some value");

        when(logger.isDebugEnabled()).thenReturn(true);

        new TraceIdFilter().logAllHeaders(request);

        verify(logger).debug("Request headers: test=[test123|123test],x-test=[some value]");
        verifyNoMoreInteractions(logger);
    }

    @Test
    @DisplayName("logAllHeaders() should log mask sensitive header values")
    void logHeadersMasksSensitiveHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("authorization", "123test");

        when(logger.isDebugEnabled()).thenReturn(true);

        new TraceIdFilter().logAllHeaders(request);

        verify(logger).debug("Request headers: authorization=[***]");
        verifyNoMoreInteractions(logger);
    }

    @Test
    @DisplayName("addAdditionalHeaders() when using default Customizer, no headers should be added to MDC")
    void testNoAdditionalHeadersAreAddedToMdc() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "123test");

        new TraceIdFilter().addAdditionalHeaders(request);

        verifyNoInteractions(mdcMock);
        verifyNoInteractions(logger);
    }

    @Test
    @DisplayName("addAdditionalHeaders() when using custom Customizer, selected headers should be added to MDC")
    void testAdditionalHeadersWithSingleValueAreAddedToMdc() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "123test");
        request.addHeader("x-test", "321test");

        TraceIdCustomizer customizer = TraceIdCustomizerImpl.builder().additionalHeader("test").build();
        new TraceIdFilter(customizer).addAdditionalHeaders(request);

        verify(mdcMock).put("test", "123test");
        verifyNoMoreInteractions(mdcMock);
        verifyNoInteractions(logger);
    }

    @Test
    @DisplayName("addAdditionalHeaders() when using custom Customizer, selected header with multiple values should be added to MDC")
    void testAdditionalHeadersWithMultipleValuesAreAddedToMdc() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("test", "123test");
        request.addHeader("test", "test123");

        TraceIdCustomizer customizer = TraceIdCustomizerImpl.builder().additionalHeader("test").build();
        new TraceIdFilter(customizer).addAdditionalHeaders(request);

        verify(mdcMock).put("test", "123test|test123");
        verifyNoMoreInteractions(mdcMock);
        verifyNoInteractions(logger);
    }

    @Test
    @DisplayName("addAdditionalHeaders() when using custom Customizer, logs debug message when header is not available")
    void testAdditionalHeadersLogsWhenHeaderIsNotFoundInRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        when(logger.isDebugEnabled()).thenReturn(true);

        TraceIdCustomizer customizer = TraceIdCustomizerImpl.builder().additionalHeader("test").build();
        new TraceIdFilter(customizer).addAdditionalHeaders(request);

        verifyNoInteractions(mdcMock);
        verify(logger).debug("Header with name '{}' not present in request", "test");
        verifyNoMoreInteractions(logger);
    }

    @Test
    @DisplayName("addForwardingInfo() copies multiple IP addresses from X-Forwarded-For header to MDC")
    void testAddIpAddressesUsesXForwardedForHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("x-forwarded-for", "192.168.69.145,192.168.69.1");

        new TraceIdFilter().addForwardingInfo(request);

        verify(mdcMock).put("x_forwarded_for", "192.168.69.145,192.168.69.1");
        verifyNoMoreInteractions(mdcMock);
        verifyNoInteractions(logger);
    }

    @Test
    @DisplayName("addForwardingInfo() copies multiple IP addresses from X-Forwarded-For headers to MDC")
    void testAddIpAddressesUsesXForwardedForHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("x-forwarded-for", "192.168.69.145");
        request.addHeader("x-forwarded-for", "192.168.69.1");

        new TraceIdFilter().addForwardingInfo(request);

        verify(mdcMock).put("x_forwarded_for", "192.168.69.145|192.168.69.1");
        verifyNoMoreInteractions(mdcMock);
        verifyNoInteractions(logger);
    }

    @Test
    @DisplayName("addForwardingInfo() parses Forwarded header and adds info to MDC")
    void testAddForwardingHeadersParsesForwardedHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.addHeader("forwarded", "for=192.0.2.43,for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com;secret=ruewiu");

        new TraceIdFilter().addForwardingInfo(request);

        verify(mdcMock).put("forwarded", "for=192.0.2.43,for=198.51.100.17;by=203.0.113.60;proto=http;host=example.com;secret=ruewiu");
        verify(mdcMock).put("forwarded_by", "203.0.113.60");
        verify(mdcMock).put("forwarded_for", "192.0.2.43|198.51.100.17");
        verify(mdcMock).put("forwarded_host", "example.com");
        verify(mdcMock).put("forwarded_proto", "http");
        verify(mdcMock).put("forwarded_extensions", "secret=ruewiu");
        verifyNoMoreInteractions(mdcMock);
        verifyNoInteractions(logger);
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

        when(logger.isDebugEnabled()).thenReturn(false);
        when(mdcMock.get("trace_id")).thenReturn("generated_mock");

        new TraceIdFilter(new MockTraceIdProvider()).doFilter(request, response, chain);

        assertEquals("generated_mock", response.getHeader("X-Trace-ID"));

        verify(mdcMock).put("trace_id", "generated_mock");
        verify(mdcMock).put("path", "/this");
        verify(mdcMock).put("url", "http://localhost/this");
        verify(mdcMock).put("method", "GET");
        verify(mdcMock).put("query_string", null);
        verify(mdcMock).put("user_agent", "MISSING");
        verify(mdcMock).clear();
        verifyNoMoreInteractions(mdcMock);

        verify(chain).doFilter(request, response);
        verifyNoMoreInteractions(chain);

        verifyNoMoreInteractions(logger);
    }

    static class MockTraceIdProvider implements TraceIdProvider {

        @Override
        public String generate(HttpServletRequest request) {
            return "generated_mock";
        }
    }
}

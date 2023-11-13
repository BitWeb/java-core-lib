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

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TraceIdFilterTest {

    @Mock
    FilterChain chain;

    @BeforeEach
    void beforeEach() {
        MDC.clear();
    }

    @Test
    @DisplayName("doFilter() does everything needed")
    void testDoFilter() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/this");
        request.setServletPath("/this");

        MockHttpServletResponse response = new MockHttpServletResponse();

        try (MockedStatic<MDC> mdcMock = Mockito.mockStatic(MDC.class)) {
            mdcMock.when(() -> MDC.get("trace_id")).thenReturn("generated_mock");

            createFilter().doFilter(request, response, chain);

            assertEquals("generated_mock", response.getHeader("X-Trace-ID"));
            mdcMock.verify(() -> MDC.put("trace_id", "generated_mock"));

            mdcMock.verify(() -> MDC.get("trace_id"), times(2));

            mdcMock.verify(MDC::clear);
            mdcMock.verifyNoMoreInteractions();

            verify(chain).doFilter(request, response);
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

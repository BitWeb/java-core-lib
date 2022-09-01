package ee.bitweb.core.trace;

import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import ee.bitweb.core.trace.invoker.InvokerTraceIdFormConfig;
import ee.bitweb.core.trace.invoker.http.HttpServletRequestTraceIdResolver;
import ee.bitweb.core.trace.invoker.http.TraceIdFilter;
import ee.bitweb.core.trace.invoker.http.TraceIdFilterConfig;
import ee.bitweb.core.trace.invoker.scheduler.SchedulerTraceIdFormConfig;
import ee.bitweb.core.trace.invoker.scheduler.SchedulerTraceIdResolver;
import ee.bitweb.core.trace.thread.ThreadTraceIdFormConfig;
import ee.bitweb.core.trace.thread.ThreadTraceIdResolver;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        properties = {
                "ee.bitweb.core.trace.auto-configuration=true",
                "ee.bitweb.core.trace.invoker.delimiter=/",
                "ee.bitweb.core.trace.invoker.length=15",
                "ee.bitweb.core.trace.invoker.prefix=invoker-prefix",
                "ee.bitweb.core.trace.thread.delimiter=#",
                "ee.bitweb.core.trace.thread.length=8",
                "ee.bitweb.core.trace.thread.prefix=thread-",
                "ee.bitweb.core.trace.scheduler.length=18",
                "ee.bitweb.core.trace.scheduler.prefix=scheduler-prefix",
                "ee.bitweb.core.trace.invoker.http.headerName=Custom-ID",
                "ee.bitweb.core.trace.invoker.http.additionalHeaders[0].contextKey=contextKey1",
                "ee.bitweb.core.trace.invoker.http.additionalHeaders[0].headerName=customHeaderName1",
                "ee.bitweb.core.trace.invoker.http.additionalHeaders[1].contextKey=contextKey2",
                "ee.bitweb.core.trace.invoker.http.additionalHeaders[1].headerName=customHeaderName2",
                "ee.bitweb.core.trace.invoker.http.enabledFeatures=ADD_URL,ADD_METHOD,ADD_USER_AGENT"
        }
)
public class AutoConfigurationTests {

    @Autowired
    private TraceIdFilter filter;

    @Autowired
    private TraceIdFilterConfig traceIdFilterConfig;

    @Autowired
    private InvokerTraceIdFormConfig invokerTraceIdFormConfig;

    @Autowired
    private SchedulerTraceIdFormConfig schedulerTraceIdFormConfig;

    @Autowired
    private ThreadTraceIdFormConfig threadTraceIdFormConfig;

    @Autowired
    @Qualifier("InvokerTraceIdCreator")
    private TraceIdCreator invokerTraceIdCreator;

    @Autowired
    @Qualifier("SchedulerTraceIdCreator")
    private TraceIdCreator schedulerTraceIdCreator;

    @Autowired
    @Qualifier("ThreadTraceIdCreator")
    private TraceIdCreator threadTraceIdCreator;

    @Autowired
    private HttpServletRequestTraceIdResolver httpServletRequestTraceIdResolver;

    @Autowired
    private SchedulerTraceIdResolver schedulerTraceIdResolver;

    @Autowired
    private ThreadTraceIdResolver threadTraceIdResolver;

    @Autowired
    private TraceIdContext context;

    @Mock
    HttpServletRequest request;

    @Test
    void onEnabledAutoConfigurationIsCorrect() {
        assertFilter();
        assertInvokerTraceIdCreator();
        assertTraceIdFilterConfig();
        assertHttpServletRequestTraceIdResolver();
        assertInvokerTraceIdFormConfig();
        assertSchedulerTraceIdCreator();
        assertSchedulerTraceIdFormConfig();
        assertThreadTraceIdCreator();
        assertThreadTraceIdFormConfig();
        assertThreadTraceIdResolver();
        assertHttpServletRequestTraceIdResolver();
        assertSchedulerTraceIdResolver();
    }

    private void assertFilter() {
        assertNotNull(filter);
    }

    private void assertTraceIdFilterConfig() {
        assertNotNull(traceIdFilterConfig);
        assertEquals("Custom-ID", traceIdFilterConfig.getHeaderName());
        assertTrue(traceIdFilterConfig.getEnabledFeatures().contains(TraceIdFilter.Feature.ADD_URL));
        assertTrue(traceIdFilterConfig.getEnabledFeatures().contains(TraceIdFilter.Feature.ADD_METHOD));
        assertTrue(traceIdFilterConfig.getEnabledFeatures().contains(TraceIdFilter.Feature.ADD_USER_AGENT));
        assertEquals(3, traceIdFilterConfig.getEnabledFeatures().size());
    }

    private void assertInvokerTraceIdCreator() {
        assertNotNull(invokerTraceIdCreator);
        assertEquals(15, invokerTraceIdCreator.generate(null).length());
        assertTrue(invokerTraceIdCreator.generate("a").startsWith("a/invoker-prefix"));
    }

    private void assertInvokerTraceIdFormConfig() {
        assertNotNull(invokerTraceIdFormConfig);
        assertEquals('/', invokerTraceIdFormConfig.getDelimiter());
        assertEquals("invoker-prefix", invokerTraceIdFormConfig.getPrefix());
        assertEquals(15, invokerTraceIdFormConfig.getLength());
    }

    private void assertHttpServletRequestTraceIdResolver() {
        MDC.clear();
        assertNotNull(httpServletRequestTraceIdResolver);
        Mockito.doReturn("inbound-id").when(request).getHeader("Custom-ID");
        httpServletRequestTraceIdResolver.resolve(request);

        assertTrue(context.get().startsWith("inbound-id/invoker-prefix"));
        assertEquals(26, context.get().length());
        MDC.clear();
    }

    private void assertSchedulerTraceIdResolver() {
        MDC.clear();
        assertNotNull(schedulerTraceIdResolver);
        schedulerTraceIdResolver.resolve();
        assertTrue(context.get().startsWith("scheduler-prefix"));
        assertEquals(18, context.get().length());
        MDC.clear();
    }

    private void assertThreadTraceIdResolver() {
        MDC.clear();
        context.set("existing-trace-id");
        assertNotNull(threadTraceIdResolver);
        threadTraceIdResolver.resolve();
        assertTrue(context.get().startsWith("existing-trace-id#thread-"));
        assertEquals(26, context.get().length());
        MDC.clear();
    }

    private void assertSchedulerTraceIdCreator() {
        assertNotNull(schedulerTraceIdCreator);
        assertEquals(18, schedulerTraceIdCreator.generate(null).length());
        assertTrue(schedulerTraceIdCreator.generate(null).startsWith("scheduler-prefix"));
    }

    private void assertSchedulerTraceIdFormConfig() {
        assertNotNull(schedulerTraceIdFormConfig);
        assertEquals("scheduler-prefix", schedulerTraceIdFormConfig.getPrefix());
        assertEquals(18, schedulerTraceIdFormConfig.getLength());
    }

    private void assertThreadTraceIdCreator() {
        assertNotNull(threadTraceIdCreator);
        assertEquals(8, threadTraceIdCreator.generate(null).length());
        assertTrue(threadTraceIdCreator.generate("a").startsWith("a#thread-"));
    }

    private void assertThreadTraceIdFormConfig() {
        assertNotNull(threadTraceIdFormConfig);
        assertEquals('#', threadTraceIdFormConfig.getDelimiter());
        assertEquals("thread-", threadTraceIdFormConfig.getPrefix());
        assertEquals(8, threadTraceIdFormConfig.getLength());
    }
}

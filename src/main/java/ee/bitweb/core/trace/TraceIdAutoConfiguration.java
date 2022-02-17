package ee.bitweb.core.trace;

import ee.bitweb.core.trace.context.MDCTraceIdContext;
import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import ee.bitweb.core.trace.creator.TraceIdCreatorImpl;
import ee.bitweb.core.trace.invoker.InvokerTraceIdFormConfig;
import ee.bitweb.core.trace.invoker.http.HttpServletRequestTraceIdResolver;
import ee.bitweb.core.trace.invoker.http.TraceIdFilter;
import ee.bitweb.core.trace.invoker.http.TraceIdFilterConfig;
import ee.bitweb.core.trace.invoker.scheduler.SchedulerTraceIdFormConfig;
import ee.bitweb.core.trace.invoker.scheduler.SchedulerTraceIdResolver;
import ee.bitweb.core.trace.thread.ThreadTraceIdFormConfig;
import ee.bitweb.core.trace.thread.ThreadTraceIdResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(
        {
                InvokerTraceIdFormConfig.class,
                ThreadTraceIdFormConfig.class,
                SchedulerTraceIdFormConfig.class,
                TraceIdFilterConfig.class
        }
)
@ConditionalOnProperty(value = "ee.bitweb.core.trace.auto-configuration", havingValue = "true")
public class TraceIdAutoConfiguration {

    private final InvokerTraceIdFormConfig invokerTraceIdFormConfig;
    private final ThreadTraceIdFormConfig threadTraceIdFormConfig;
    private final SchedulerTraceIdFormConfig schedulerTraceIdFormConfig;
    private final TraceIdFilterConfig traceIdFilterConfig;

    @Bean("InvokerTraceIdCreator")
    @ConditionalOnMissingBean(name="InvokerTraceIdCreator")
    public TraceIdCreatorImpl defaultInvokerTraceIdCreator() {
        return new TraceIdCreatorImpl(invokerTraceIdFormConfig);
    }

    @Bean("ThreadTraceIdCreator")
    @ConditionalOnMissingBean(name="ThreadTraceIdCreator")
    public TraceIdCreator defaultThreadTraceIdCreator() {
        return new TraceIdCreatorImpl(threadTraceIdFormConfig);
    }

    @Bean("SchedulerTraceIdCreator")
    @ConditionalOnMissingBean(name="SchedulerTraceIdCreator")
    public TraceIdCreatorImpl defaultSchedulerTraceIdCreator() {
        return new TraceIdCreatorImpl(schedulerTraceIdFormConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpServletRequestTraceIdResolver httpTraceIdResolver(
            @Qualifier("InvokerTraceIdCreator") TraceIdCreator creator,
            TraceIdContext context
    ) {
        return new HttpServletRequestTraceIdResolver(
                traceIdFilterConfig.getHeaderName(),
                context,
                creator

        );
    }

    @Bean
    @ConditionalOnMissingBean
    public ThreadTraceIdResolver threadTraceIdResolver(
            TraceIdContext context,
            @Qualifier("ThreadTraceIdCreator") TraceIdCreator creator
    ) {
        return new ThreadTraceIdResolver(context, creator);
    }

    @Bean
    @ConditionalOnMissingBean
    public SchedulerTraceIdResolver schedulerTraceIdResolver(
            TraceIdContext context,
            @Qualifier("SchedulerTraceIdCreator") TraceIdCreator creator
    ) {
        return new SchedulerTraceIdResolver(context, creator);
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceIdContext traceIdContext() {
        return MDCTraceIdContext.standard();
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceIdFilter traceIdFilter(HttpServletRequestTraceIdResolver resolver, TraceIdContext context) {
        return new TraceIdFilter(traceIdFilterConfig, context, resolver);
    }
}

package ee.bitweb.core.trace;

import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import ee.bitweb.core.trace.invoker.amqp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({AmqpTraceProperties.class})
@ConditionalOnProperty(value = "ee.bitweb.core.trace.auto-configuration", havingValue = "true")
@ConditionalOnClass(MessagePostProcessor.class)
public class TraceIdAmqpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AmqpTraceIdResolver amqpTraceIdResolver(
            @Qualifier("InvokerTraceIdCreator") TraceIdCreator traceIdCreator,
            TraceIdContext context,
            AmqpTraceProperties properties
    ) {
        log.info("Creating default AmqpTraceIdResolver");

        return new AmqpTraceIdResolver(properties, traceIdCreator, context);
    }

    @Bean
    @ConditionalOnMissingBean
    public AmqpTraceBeforePublishMessageProcessor amqpTraceBeforePublishMessageProcessor(
            TraceIdContext context,
            AmqpTraceProperties properties
    ) {
        log.info("Creating default AmqpTraceBeforePublishMessageProcessor");

        return new AmqpTraceBeforePublishMessageProcessor(properties, context);
    }

    @Bean
    @ConditionalOnMissingBean
    public AmqpTraceAfterReceiveMessageProcessor amqpTraceAfterReceiveMessageProcessor(
            TraceIdContext context
    ) {
        log.info("Creating default AmqpTraceAfterReceiveMessageProcessor");

        return new AmqpTraceAfterReceiveMessageProcessor(context);
    }

    @Bean
    @ConditionalOnMissingBean
    public AmqpTraceAdvisor amqpTraceAdvisor(AmqpTraceIdResolver resolver) {
        log.info("Creating default AmqpTraceAdvisor");

        return new AmqpTraceAdvisor(resolver);
    }
}

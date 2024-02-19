package ee.bitweb.core.trace;

import ee.bitweb.core.amqp.CoreExceptionStrategy;
import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import ee.bitweb.core.trace.invoker.amqp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({AmqpTraceProperties.class})
@ConditionalOnProperty(value = TraceIdProperties.PREFIX + ".auto-configuration", havingValue = "true")
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
    public AmqpTraceAdvisor amqpTraceAdvisor(AmqpTraceIdResolver resolver, TraceIdContext context, Optional<ConditionalRejectingErrorHandler> errorHandler) {
        log.info("Creating default AmqpTraceAdvisor");

        ConditionalRejectingErrorHandler handler = errorHandler.orElse(null);

        return new AmqpTraceAdvisor(resolver, context, !(handler instanceof AmqpTraceAwareExceptionHandler));
    }

    @Bean
    @ConditionalOnMissingBean
    public ConditionalRejectingErrorHandler amqpTraceAwareExceptionHandler(TraceIdContext context) {
        log.info("Creating default AmqpTraceAwareExceptionHandler");

        ConditionalRejectingErrorHandler handler = new AmqpTraceAwareExceptionHandler(
                context,
                new CoreExceptionStrategy()
        );
        handler.setDiscardFatalsWithXDeath(false);

        return handler;
    }
}

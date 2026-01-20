package ee.bitweb.core.amqp;

import tools.jackson.databind.json.JsonMapper;
import ee.bitweb.core.trace.invoker.amqp.AmqpTraceAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
@ConditionalOnClass(ConnectionFactory.class)
@ConditionalOnProperty(value = AmqpProperties.PREFIX + ".auto-configuration", havingValue = "true")
public class AmqpAutoConfiguration {

    @Bean
    public AmqpService amqpService(RabbitTemplate template) {
        return new AmqpService(template);
    }

    @Bean(RabbitListenerAnnotationBeanPostProcessor.DEFAULT_RABBIT_LISTENER_CONTAINER_FACTORY_BEAN_NAME)
    @ConditionalOnMissingBean
    public SimpleRabbitListenerContainerFactory jsaFactory(
            ConnectionFactory connectionFactory,
            MessageConverter converter,
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            List<AmqpListenerInterceptor> interceptors,
            Optional<ConditionalRejectingErrorHandler> errorHandler
    ) {
        log.info("Creating a default SimpleRabbitListenerContainerFactory");
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(converter);

        factory.setErrorHandler(errorHandler.orElse(createDefaultErrorHandler()));

        var sortedInterceptors = sortTraceIdInterceptorToFirst(interceptors);
        for (AmqpListenerInterceptor interceptor : sortedInterceptors) {
            log.info("Adding {} to SimpleRabbitListenerContainerFactory", interceptor.getClass().getSimpleName());
        }
        factory.setAdviceChain(sortedInterceptors.toArray(new MethodInterceptor[0]));

        return factory;
    }

    private List<AmqpListenerInterceptor> sortTraceIdInterceptorToFirst(List<AmqpListenerInterceptor> interceptors) {
        List<AmqpListenerInterceptor> sorted = new ArrayList<>();

        var traceIdInterceptor = interceptors.stream().filter(AmqpTraceAdvisor.class::isInstance)
                .findFirst()
                .orElse(null);

        if (traceIdInterceptor != null) {
            sorted.add(traceIdInterceptor);
            interceptors.remove(traceIdInterceptor);
        }

        sorted.addAll(interceptors);

        return sorted;
    }

    private ConditionalRejectingErrorHandler createDefaultErrorHandler() {
        ConditionalRejectingErrorHandler errorHandler = new ConditionalRejectingErrorHandler(
                new CoreExceptionStrategy()
        );
        errorHandler.setDiscardFatalsWithXDeath(false);

        return errorHandler;
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter converter,
            List<AmqpBeforePublishMessageProcessor> processors
    ) {
        log.info("Creating a default RabbitTemplate");
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);

        for (AmqpBeforePublishMessageProcessor processor : processors) {
            log.info("Adding {} to RabbitTemplate", processor.getClass().getSimpleName());
        }
        template.setBeforePublishPostProcessors(processors.toArray(new AmqpBeforePublishMessageProcessor[0]));

        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageConverter jsonMessageConverter(JsonMapper mapper) {
        return new JacksonJsonMessageConverter(mapper);
    }
}

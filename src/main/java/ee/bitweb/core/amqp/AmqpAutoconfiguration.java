package ee.bitweb.core.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.trace.invoker.amqp.AmqpTraceAdvisor;
import ee.bitweb.core.trace.invoker.amqp.AmqpTraceBeforePublishMessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

import java.util.Optional;

@Slf4j
@Configuration
@ConditionalOnClass(ConnectionFactory.class)
@ConditionalOnProperty(value = "ee.bitweb.core.amqp.auto-configuration", havingValue = "true")
public class AmqpAutoconfiguration {

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
            Optional<AmqpTraceAdvisor> traceAdvisor
    ) {
        log.info("Creating a default SimpleRabbitListenerContainerFactory");
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(converter);

        factory.setErrorHandler(createDefaultErrorHandler());
        traceAdvisor.ifPresent(
                advisor -> {
                    log.info("Adding AMQP Trace Advisor to SimpleRabbitListenerContainerFactory");
                    factory.setAdviceChain(advisor);
                }
        );

        return factory;
    }

    private ErrorHandler createDefaultErrorHandler() {
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
            Optional<AmqpTraceBeforePublishMessageProcessor> processor
    ) {
        log.info("Creating a default RabbitTemplate");
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        processor.ifPresent(template::setBeforePublishPostProcessors);

        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageConverter jsonMessageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }
}

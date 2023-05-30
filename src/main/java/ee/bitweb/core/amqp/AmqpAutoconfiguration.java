package ee.bitweb.core.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
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

import java.util.List;

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
            List<AmqpListenerInterceptor> interceptors
    ) {
        log.info("Creating a default SimpleRabbitListenerContainerFactory");
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(converter);

        factory.setErrorHandler(createDefaultErrorHandler());

        interceptors.forEach(interceptor ->
                log.info("Adding {} to SimpleRabbitListenerContainerFactory", interceptor.getClass().getSimpleName())
        );
        factory.setAdviceChain(interceptors.toArray(new MethodInterceptor[0]));

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
            List<AmqpBeforePublishMessageProcessor> processors
    ) {
        log.info("Creating a default RabbitTemplate");
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);

        processors.forEach(processor ->
                log.info("Adding {} to RabbitTemplate", processor.getClass().getSimpleName())
        );
        template.setBeforePublishPostProcessors(processors.toArray(new AmqpBeforePublishMessageProcessor[0]));

        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageConverter jsonMessageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }
}

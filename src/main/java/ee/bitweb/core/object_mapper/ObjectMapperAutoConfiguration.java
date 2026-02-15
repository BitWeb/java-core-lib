package ee.bitweb.core.object_mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ee.bitweb.core.object_mapper.deserializer.Jackson2TrimmedStringDeserializer;
import ee.bitweb.core.object_mapper.deserializer.TrimmedStringDeserializer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;

/**
 * Auto-configuration for ObjectMapper (Jackson 2) and JsonMapper (Jackson 3).
 *
 * <p>Both are configured with identical behavior:</p>
 * <ul>
 *   <li>TrimmedStringDeserializer - trims whitespace from all string fields</li>
 *   <li>ADJUST_DATES_TO_CONTEXT_TIME_ZONE disabled</li>
 *   <li>ACCEPT_FLOAT_AS_INT disabled</li>
 * </ul>
 *
 * <p>Jackson 2 ObjectMapper is required for Retrofit's converter-jackson.</p>
 * <p>Jackson 3 JsonMapper is used by Spring Boot 4.</p>
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({ObjectMapperProperties.class})
@ConditionalOnProperty(value = ObjectMapperProperties.PREFIX + ".auto-configuration", havingValue = "true")
public class ObjectMapperAutoConfiguration {

    /**
     * Jackson 3 JsonMapper customizer for Spring Boot 4.
     * Extends Spring Boot's auto-configured JsonMapper.
     */
    @Bean
    public JsonMapperBuilderCustomizer coreLibJsonMapperCustomizer() {
        log.info("Applying Core Library JsonMapper (Jackson 3) customizations");

        return builder -> builder
                .addModule(TrimmedStringDeserializer.createModule())
                .disable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
    }

    /**
     * Jackson 2 ObjectMapper customizer for Retrofit compatibility.
     * Configures the ObjectMapper bean with the same behavior as JsonMapper.
     */
    @Slf4j
    @Configuration
    @RequiredArgsConstructor
    @ConditionalOnBean(ObjectMapper.class)
    static class Jackson2ObjectMapperCustomizer {

        private final ObjectMapper objectMapper;

        @PostConstruct
        public void customize() {
            log.info("Applying Core Library ObjectMapper (Jackson 2) customizations");

            Jackson2TrimmedStringDeserializer.addToObjectMapper(objectMapper);
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
            objectMapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        }
    }
}

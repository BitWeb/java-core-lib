package ee.bitweb.core.object_mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ee.bitweb.core.object_mapper.deserializer.TrimmedStringDeserializer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = ObjectMapperProperties.PREFIX + ".auto-configuration", havingValue = "true")
public class ObjectMapperAutoConfiguration {

    private final ObjectMapper mapper;

    @PostConstruct
    public void init() {
        log.info("ObjectMapper AutoConfiguring executed");

        TrimmedStringDeserializer.addToObjectMapper(mapper);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        mapper.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
    }
}

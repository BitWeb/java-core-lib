package ee.bitweb.core;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

@SpringBootApplication
@EnableConfigurationProperties
public class TestSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestSpringApplication.class, args);
    }

    @RequiredArgsConstructor
    @org.springframework.context.annotation.Configuration
    public class Configuration {

        private final ObjectMapper mapper;

        @PostConstruct
        public void init() {
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
            mapper.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        }

        @Bean("InvokerTraceIdCreator")
        @Profile("MockedInvokerTraceIdCreator")
        public TraceIdCreator creator() {
            return new MockedCreator();
        }

        public class MockedCreator implements TraceIdCreator {

            @Override
            public String generate(String traceId) {
                return (StringUtils.hasText(traceId) ? traceId  + "_": "") + "generated-trace-id";
            }
        }
    }
}

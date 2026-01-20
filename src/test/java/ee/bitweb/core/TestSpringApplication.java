package ee.bitweb.core;


import tools.jackson.databind.json.JsonMapper;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.util.StringUtils;

@SpringBootApplication
@EnableConfigurationProperties
public class TestSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestSpringApplication.class);
    }

    @org.springframework.context.annotation.Configuration
    public static class Configuration {

        @Bean
        public tools.jackson.databind.ObjectMapper jackson3ObjectMapper() {
            // Jackson 3.x for Spring Boot 4's internal use
            return JsonMapper.builder()
                    .disable(tools.jackson.databind.DeserializationFeature.ACCEPT_FLOAT_AS_INT)
                    .build();
        }

        @Bean
        public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
            // Jackson 2.x for test compatibility
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
            mapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_FLOAT_AS_INT);
            return mapper;
        }

        @Bean("InvokerTraceIdCreator")
        @Profile("MockedInvokerTraceIdCreator")
        public TraceIdCreator creator() {
            return new MockedCreator();
        }

        public static class MockedCreator implements TraceIdCreator {

            @Override
            public String generate(String traceId) {
                return (StringUtils.hasText(traceId) ? traceId  + "_": "") + "generated-trace-id";
            }
        }
    }

    @org.springframework.context.annotation.Configuration
    public static class SecurityConfiguration {

        @Bean
        protected SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
            // Configure security to allow any request other than actuator requests

            return httpSecurity
                    .csrf(AbstractHttpConfigurer::disable)
                    .securityMatcher(new NegatedRequestMatcher(EndpointRequest.toAnyEndpoint()))
                    .authorizeHttpRequests(requests -> requests.anyRequest().hasAnyRole("ANONYMOUS"))
                    .httpBasic(Customizer.withDefaults()).build();
        }
    }
}

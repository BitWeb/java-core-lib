package ee.bitweb.core;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
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

    @RequiredArgsConstructor
    @org.springframework.context.annotation.Configuration
    public static class Configuration {

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

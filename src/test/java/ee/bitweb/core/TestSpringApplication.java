package ee.bitweb.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.trace.creator.TraceIdCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.util.StringUtils;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@SpringBootApplication
@EnableConfigurationProperties
public class TestSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestSpringApplication.class);
    }

    @Configuration
    public static class JacksonConfiguration {

        @Bean
        public JsonMapper jackson3ObjectMapper() {
            return JsonMapper.builder()
                    .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
                    .build();
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
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

    @Configuration
    public static class SecurityConfiguration {

        @Bean
        protected SecurityFilterChain configure(HttpSecurity httpSecurity) {
            // Configure security to allow any request other than actuator requests

            return httpSecurity
                    .csrf(AbstractHttpConfigurer::disable)
                    .securityMatcher(new NegatedRequestMatcher(EndpointRequest.toAnyEndpoint()))
                    .authorizeHttpRequests(requests -> requests.anyRequest().hasAnyRole("ANONYMOUS"))
                    .httpBasic(Customizer.withDefaults()).build();
        }
    }
}

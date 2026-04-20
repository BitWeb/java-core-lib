package ee.bitweb.core.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j(topic = "ee.bitweb.core.actuator")
@Order(110)
@Configuration
@ConditionalOnProperty(value = ActuatorSecurityProperties.PREFIX + ".auto-configuration", havingValue = "true")
@RequiredArgsConstructor
public class ActuatorSecurity {

    private final ActuatorSecurityProperties actuatorSecurityProperties;

    @Bean
    public SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
        String allowedRole = actuatorSecurityProperties.getRole();
        http
                .securityMatcher(EndpointRequest.toAnyEndpoint().excluding("health"))
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(new ActuatorAuthenticationProvider(actuatorSecurityProperties))
                .authorizeHttpRequests(requests -> requests.anyRequest().hasRole(allowedRole))
                .httpBasic(Customizer.withDefaults());

        log.info("Configured security for actuator endpoints excluding health, allowing roles {}", allowedRole);

        return http.build();
    }
}

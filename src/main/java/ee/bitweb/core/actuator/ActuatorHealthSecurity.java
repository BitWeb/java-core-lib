package ee.bitweb.core.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointProperties;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.boot.actuate.endpoint.Show;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import java.util.List;

@Slf4j(topic = "ee.bitweb.core.actuator")
@Order(111)
@Configuration
@ConditionalOnProperty(value = ActuatorSecurityProperties.PREFIX + ".auto-configuration", havingValue = "true")
@RequiredArgsConstructor
public class ActuatorHealthSecurity {

    private final ActuatorSecurityProperties actuatorSecurityProperties;

    private final HealthEndpointProperties healthEndpointProperties;

    protected void configure(HttpSecurity httpSecurity) {
        List<String> allowedRoles = actuatorSecurityProperties.getHealthEndpointRoles();

        logUnsafeHealthEndpointWarning();

        httpSecurity
                .securityMatcher(EndpointRequest.to("health"))
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(new ActuatorAuthenticationProvider(actuatorSecurityProperties))
                .authorizeHttpRequests(requests -> requests.anyRequest().hasAnyRole(allowedRoles.toArray(new String[0])))
                .httpBasic(Customizer.withDefaults());

        log.info("Configured actuator security for health endpoint, allowing roles {}", allowedRoles);
    }

    private void logUnsafeHealthEndpointWarning() {
        if (actuatorSecurityProperties.isDisableUnsafeHealthEndpointWarning()) {
            return;
        }

        if (!healthEndpointProperties.getShowDetails().equals(Show.ALWAYS)) {
            return;
        }

        log.warn(
                "Detected potentially unsafe configuration, please make sure that no sensitive information leaks from" +
                        " health endpoint or set 'management.endpoint.health.show-details' to 'when_authorized'!"
        );
    }
}

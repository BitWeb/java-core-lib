package ee.bitweb.core.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.health.HealthProperties;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.List;

@Slf4j(topic = "ee.bitweb.core.actuator")
@Order(111)
@Configuration
@ConditionalOnProperty(value = "ee.bitweb.core.actuator.security.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ActuatorHealthSecurity extends WebSecurityConfigurerAdapter {

    private final ActuatorSecurityProperties actuatorSecurityProperties;

    private final HealthEndpointProperties healthEndpointProperties;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        List<String> allowedRoles = actuatorSecurityProperties.getHealthEndpointRoles();

        logUnsafeHealthEndpointWarning();

        httpSecurity
                .requestMatcher(EndpointRequest.to("health"))
                .csrf().disable()
                .authenticationProvider(new ActuatorAuthenticationProvider(actuatorSecurityProperties))
                .authorizeRequests(requests -> requests.anyRequest().hasAnyRole(allowedRoles.toArray(new String[0])))
                .httpBasic();

        log.info("Configured actuator security for health endpoint, allowing roles {}", allowedRoles);
    }

    private void logUnsafeHealthEndpointWarning() {
        if (actuatorSecurityProperties.getDisableUnsafeHealthEndpointWarning()) {
            return;
        }

        if (!healthEndpointProperties.getShowDetails().equals(HealthProperties.Show.ALWAYS)) {
            return;
        }

        log.warn(
                "Detected potentially unsafe configuration, please make sure that no sensitive information leaks from" +
                        " health endpoint or set 'management.endpoint.health.show-details' to 'when_authorized'!"
        );
    }
}

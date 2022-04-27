package ee.bitweb.core.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Slf4j(topic = "ee.bitweb.core.actuator")
@Order(110)
@Configuration
@ConditionalOnProperty(value = "ee.bitweb.core.actuator.security.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ActuatorSecurity extends WebSecurityConfigurerAdapter {

    private final ActuatorSecurityProperties actuatorSecurityProperties;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        String allowedRole = actuatorSecurityProperties.getRole();

        httpSecurity
                .requestMatcher(EndpointRequest.toAnyEndpoint().excluding("health"))
                .csrf().disable()
                .authenticationProvider(new ActuatorAuthenticationProvider(actuatorSecurityProperties))
                .authorizeRequests(requests -> requests.anyRequest().hasRole(allowedRole))
                .httpBasic();

        log.info("Configured security for actuator endpoints excluding health, allowing roles {}", allowedRole);
    }
}

package ee.bitweb.core.actuator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j(topic = "ee.bitweb.core.actuator")
@RequiredArgsConstructor
public class ActuatorAuthenticationProvider implements AuthenticationProvider {

    private final ActuatorSecurityProperties actuatorSecurityProperties;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var userName = ((String) authentication.getPrincipal());
        var password = ((String) authentication.getCredentials());

        var user = actuatorSecurityProperties.getUser();

        if (!user.getName().equals(userName)) {
            log.debug("Invalid username");
            throw new BadCredentialsException("Invalid username");
        }

        if (!user.getPassword().equals(password)) {
            log.debug("Invalid password");
            throw new BadCredentialsException("Invalid password");
        }

        log.info("Authenticated user {}", userName);

        return new UsernamePasswordAuthenticationToken(userName, password, getAuthorities());
    }

    private Collection<GrantedAuthority> getAuthorities() {
        return actuatorSecurityProperties
                .getUser()
                .getRoles()
                .stream()
                .map(role -> "ROLE_" + role.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}

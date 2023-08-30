package ee.bitweb.core.actuator;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "ee.bitweb.core.actuator.security")
@ConditionalOnProperty(value = "ee.bitweb.core.actuator.security.enabled", havingValue = "true")
public class ActuatorSecurityProperties {

    private static final String DEFAULT_ROLE = "ACTUATOR";

    @NotNull
    private Boolean enabled = false;

    @NotBlank
    private String role = DEFAULT_ROLE;

    @NotEmpty
    private List<String> healthEndpointRoles = List.of(DEFAULT_ROLE, "ANONYMOUS");

    @NotNull
    private Boolean disableUnsafeHealthEndpointWarning = false;

    @NotNull
    private User user = new User();

    @Getter
    @Setter
    @Validated
    public static class User {

        @NotBlank
        private String name = "actuator-user";

        @NotBlank
        private String password = UUID.randomUUID().toString();

        @NotEmpty
        private List<String> roles = List.of(DEFAULT_ROLE);
    }
}

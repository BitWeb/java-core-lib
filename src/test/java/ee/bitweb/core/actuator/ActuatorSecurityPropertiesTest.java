package ee.bitweb.core.actuator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class ActuatorSecurityPropertiesTest {

    @Test
    @DisplayName("Test default values")
    void testDefaultValues() {
        ActuatorSecurityProperties properties = new ActuatorSecurityProperties();

        assertAll(
                () -> assertFalse(properties.isAutoConfiguration()),
                () -> assertEquals("ACTUATOR", properties.getRole()),
                () -> assertEquals(2, properties.getHealthEndpointRoles().size()),
                () -> assertEquals("ACTUATOR", properties.getHealthEndpointRoles().get(0)),
                () -> assertEquals("ANONYMOUS", properties.getHealthEndpointRoles().get(1)),
                () -> assertFalse(properties.isDisableUnsafeHealthEndpointWarning()),
                () -> assertEquals("actuator-user", properties.getUser().getName()),
                () -> assertNotNull(properties.getUser().getPassword()),
                () -> assertEquals(1, properties.getUser().getRoles().size()),
                () -> assertEquals("ACTUATOR", properties.getUser().getRoles().get(0))
        );
    }
}

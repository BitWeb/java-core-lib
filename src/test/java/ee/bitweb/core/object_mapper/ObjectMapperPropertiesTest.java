package ee.bitweb.core.object_mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class ObjectMapperPropertiesTest {

    @Test
    @DisplayName("Should have correct prefix constant")
    void shouldHaveCorrectPrefixConstant() {
        assertEquals("ee.bitweb.core.object-mapper", ObjectMapperProperties.PREFIX);
    }

    @Test
    @DisplayName("Should have default autoConfiguration as false")
    void shouldHaveDefaultAutoConfigurationAsFalse() {
        ObjectMapperProperties properties = new ObjectMapperProperties();

        assertFalse(properties.getAutoConfiguration());
    }

    @Test
    @DisplayName("Should allow setting autoConfiguration to true")
    void shouldAllowSettingAutoConfigurationToTrue() {
        ObjectMapperProperties properties = new ObjectMapperProperties();

        properties.setAutoConfiguration(true);

        assertTrue(properties.getAutoConfiguration());
    }

    @Test
    @DisplayName("Should allow setting autoConfiguration back to false")
    void shouldAllowSettingAutoConfigurationBackToFalse() {
        ObjectMapperProperties properties = new ObjectMapperProperties();
        properties.setAutoConfiguration(true);

        properties.setAutoConfiguration(false);

        assertFalse(properties.getAutoConfiguration());
    }
}

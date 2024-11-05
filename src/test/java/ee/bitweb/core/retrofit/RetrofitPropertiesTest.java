package ee.bitweb.core.retrofit;

import ee.bitweb.core.retrofit.builder.LoggingLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class RetrofitPropertiesTest {

    @Test
    @DisplayName("Must return correct mappers with default config")
    void testLoggingReturnsCorrectMappersWithDefaultConfiguration() {
        RetrofitProperties.Logging properties = new RetrofitProperties.Logging();

        assertEquals(5, properties.getMappers().size());
    }

    @Test
    @DisplayName("Must return only one custom mapper with level CUSTOM")
    void testLoggingReturnsConfiguredMappersWithCustomLevel() {
        RetrofitProperties.Logging properties = new RetrofitProperties.Logging();
        properties.setLevel(LoggingLevel.CUSTOM);
        properties.getMappers().add("custom-mapper");

        assertEquals(1, properties.getMappers().size());
        assertEquals("custom-mapper", properties.getMappers().get(0));
    }

    @Test
    @DisplayName("Must add custom mappers to default mappers")
    void testLoggingReturnMergedMappersWhenAdditionalMappersAreProvided() {
        RetrofitProperties.Logging properties = new RetrofitProperties.Logging();
        properties.setMappers(List.of("custom-mapper"));

        assertEquals(6, properties.getMappers().size());
        assertTrue(properties.getMappers().contains("custom-mapper"));
    }
}

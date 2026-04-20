package ee.bitweb.core.object_mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.TestSpringApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ActiveProfiles("MockedInvokerTraceIdCreator")
@SpringBootTest(
        classes = TestSpringApplication.class,
        properties = {
                "ee.bitweb.core.object-mapper.auto-configuration=true"
        }
)
class ObjectMapperAutoConfigurationIntegrationTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JsonMapperBuilderCustomizer coreLibJsonMapperCustomizer;

    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        JsonMapper.Builder builder = JsonMapper.builder();
        coreLibJsonMapperCustomizer.customize(builder);
        jsonMapper = builder.build();
    }

    @Test
    @DisplayName("Should load ObjectMapperAutoConfiguration bean when enabled")
    void shouldLoadAutoConfigurationBeanWhenEnabled() {
        assertTrue(applicationContext.containsBean("objectMapperAutoConfiguration"));
    }

    @Test
    @DisplayName("Should load coreLibJsonMapperCustomizer bean when enabled")
    void shouldLoadCoreLibJsonMapperCustomizerBeanWhenEnabled() {
        assertTrue(applicationContext.containsBean("coreLibJsonMapperCustomizer"));
    }

    @Test
    @DisplayName("Should load Jackson2ObjectMapperCustomizer inner class bean when enabled")
    void shouldLoadJackson2ObjectMapperCustomizerBeanWhenEnabled() {
        assertTrue(applicationContext.containsBean("objectMapperAutoConfiguration.Jackson2ObjectMapperCustomizer"));
    }

    @Test
    @DisplayName("Should trim strings during deserialization")
    void shouldTrimStringsDuringDeserialization() throws JsonProcessingException {
        String json = "{\"value\": \"  trimmed  \"}";

        TestDto result = objectMapper.readValue(json, TestDto.class);

        assertEquals("trimmed", result.value);
    }

    @Test
    @DisplayName("Should handle Java time types with JavaTimeModule")
    void shouldHandleJavaTimeTypes() throws JsonProcessingException {
        String json = "{\"timestamp\": \"2024-01-15T10:30:00Z\"}";

        TestDtoWithTime result = objectMapper.readValue(json, TestDtoWithTime.class);

        assertNotNull(result.timestamp);
        assertEquals(2024, result.timestamp.getYear());
        assertEquals(1, result.timestamp.getMonthValue());
        assertEquals(15, result.timestamp.getDayOfMonth());
    }

    @Test
    @DisplayName("Should have ADJUST_DATES_TO_CONTEXT_TIME_ZONE disabled")
    void shouldHaveAdjustDatesToContextTimeZoneDisabled() {
        assertFalse(objectMapper.isEnabled(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE));
    }

    @Test
    @DisplayName("Should have ACCEPT_FLOAT_AS_INT disabled")
    void shouldHaveAcceptFloatAsIntDisabled() {
        assertFalse(objectMapper.isEnabled(DeserializationFeature.ACCEPT_FLOAT_AS_INT));
    }

    @Test
    @DisplayName("Should reject float value for integer field when ACCEPT_FLOAT_AS_INT is disabled")
    void shouldRejectFloatForIntegerField() {
        String json = "{\"count\": 1.5}";

        assertThrows(JsonProcessingException.class, () ->
                objectMapper.readValue(json, TestDtoWithInt.class)
        );
    }

    @Test
    @DisplayName("Should preserve timezone in deserialized date")
    void shouldPreserveTimezoneInDate() throws JsonProcessingException {
        String json = "{\"timestamp\": \"2024-01-15T10:30:00+05:00\"}";

        TestDtoWithTime result = objectMapper.readValue(json, TestDtoWithTime.class);

        assertEquals(ZoneOffset.ofHours(5), result.timestamp.getOffset());
    }

    @Test
    @DisplayName("Should accept valid integer for integer field")
    void shouldAcceptValidIntegerForIntegerField() throws JsonProcessingException {
        String json = "{\"count\": 42}";

        TestDtoWithInt result = objectMapper.readValue(json, TestDtoWithInt.class);

        assertEquals(42, result.count);
    }

    // Jackson 3 JsonMapper tests

    @Test
    @DisplayName("Jackson 3 JsonMapper should trim strings during deserialization")
    void jsonMapperShouldTrimStringsDuringDeserialization() throws JacksonException {
        String json = "{\"value\": \"  trimmed  \"}";

        Jackson3TestDto result = jsonMapper.readValue(json, Jackson3TestDto.class);

        assertEquals("trimmed", result.value);
    }

    @Test
    @DisplayName("Jackson 3 JsonMapper should have ADJUST_DATES_TO_CONTEXT_TIME_ZONE disabled")
    void jsonMapperShouldHaveAdjustDatesToContextTimeZoneDisabled() {
        assertFalse(jsonMapper.isEnabled(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE));
    }

    @Test
    @DisplayName("Jackson 3 JsonMapper should have ACCEPT_FLOAT_AS_INT disabled")
    void jsonMapperShouldHaveAcceptFloatAsIntDisabled() {
        assertFalse(jsonMapper.isEnabled(tools.jackson.databind.DeserializationFeature.ACCEPT_FLOAT_AS_INT));
    }

    @Test
    @DisplayName("Jackson 3 JsonMapper should reject float value for integer field")
    void jsonMapperShouldRejectFloatForIntegerField() {
        String json = "{\"count\": 1.5}";

        assertThrows(JacksonException.class, () ->
                jsonMapper.readValue(json, Jackson3TestDtoWithInt.class)
        );
    }

    @Test
    @DisplayName("Jackson 3 JsonMapper should preserve timezone in deserialized date")
    void jsonMapperShouldPreserveTimezoneInDate() throws JacksonException {
        String json = "{\"timestamp\": \"2024-01-15T10:30:00+05:00\"}";

        Jackson3TestDtoWithTime result = jsonMapper.readValue(json, Jackson3TestDtoWithTime.class);

        assertEquals(ZoneOffset.ofHours(5), result.timestamp.getOffset());
    }

    // Jackson 2 test DTOs

    static class TestDto {
        public String value;
    }

    static class TestDtoWithTime {
        public OffsetDateTime timestamp;
    }

    static class TestDtoWithInt {
        public Integer count;
    }

    // Jackson 3 test DTOs

    static class Jackson3TestDto {
        public String value;
    }

    static class Jackson3TestDtoWithTime {
        public OffsetDateTime timestamp;
    }

    static class Jackson3TestDtoWithInt {
        public Integer count;
    }
}

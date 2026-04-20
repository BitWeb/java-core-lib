package ee.bitweb.core.object_mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ObjectMapperAutoConfiguration.Jackson2ObjectMapperCreator}.
 *
 * <p>Uses {@link ApplicationContextRunner} to create a lightweight context where no pre-existing
 * {@code ObjectMapper} bean is defined, allowing the {@code @ConditionalOnMissingBean(ObjectMapper.class)}
 * condition to be satisfied and the Creator inner class to activate.</p>
 *
 * <p>This scenario is never covered by the standard {@code @SpringBootTest} integration tests because
 * {@code TestSpringApplication} always provides an {@code ObjectMapper} bean via Spring Boot auto-configuration.</p>
 */
@Tag("integration")
class Jackson2ObjectMapperCreatorIntegrationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ObjectMapperAutoConfiguration.class)
            .withPropertyValues("ee.bitweb.core.object-mapper.auto-configuration=true");

    @Test
    @DisplayName("Should load Jackson2ObjectMapperCreator inner class when no ObjectMapper exists")
    void shouldLoadJackson2ObjectMapperCreatorBean() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ObjectMapperAutoConfiguration.Jackson2ObjectMapperCreator.class);
        });
    }

    @Test
    @DisplayName("Should create ObjectMapper bean via Jackson2ObjectMapperCreator")
    void shouldCreateObjectMapperBean() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ObjectMapper.class);
            assertThat(context).hasBean("objectMapper");
        });
    }

    @Test
    @DisplayName("Created ObjectMapper should trim strings during deserialization")
    void shouldTrimStringsDuringDeserialization() {
        contextRunner.run(context -> {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
            String json = "{\"value\": \"  trimmed  \"}";

            TestDto result = objectMapper.readValue(json, TestDto.class);

            assertThat(result.value).isEqualTo("trimmed");
        });
    }

    @Test
    @DisplayName("Created ObjectMapper should handle Java time types with JavaTimeModule")
    void shouldHandleJavaTimeTypes() {
        contextRunner.run(context -> {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
            String json = "{\"timestamp\": \"2024-01-15T10:30:00Z\"}";

            TestDtoWithTime result = objectMapper.readValue(json, TestDtoWithTime.class);

            assertThat(result.timestamp).isNotNull();
            assertThat(result.timestamp.getYear()).isEqualTo(2024);
            assertThat(result.timestamp.getMonthValue()).isEqualTo(1);
            assertThat(result.timestamp.getDayOfMonth()).isEqualTo(15);
        });
    }

    @Test
    @DisplayName("Created ObjectMapper should have ADJUST_DATES_TO_CONTEXT_TIME_ZONE disabled")
    void shouldHaveAdjustDatesToContextTimeZoneDisabled() {
        contextRunner.run(context -> {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);

            assertThat(objectMapper.isEnabled(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)).isFalse();
        });
    }

    @Test
    @DisplayName("Created ObjectMapper should have ACCEPT_FLOAT_AS_INT disabled")
    void shouldHaveAcceptFloatAsIntDisabled() {
        contextRunner.run(context -> {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);

            assertThat(objectMapper.isEnabled(DeserializationFeature.ACCEPT_FLOAT_AS_INT)).isFalse();
        });
    }

    @Test
    @DisplayName("Created ObjectMapper should reject float value for integer field")
    void shouldRejectFloatForIntegerField() {
        contextRunner.run(context -> {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
            String json = "{\"count\": 1.5}";

            assertThatThrownBy(() -> objectMapper.readValue(json, TestDtoWithInt.class))
                    .isInstanceOf(JsonProcessingException.class);
        });
    }

    @Test
    @DisplayName("Created ObjectMapper should preserve timezone offset in deserialized date")
    void shouldPreserveTimezoneInDate() {
        contextRunner.run(context -> {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
            String json = "{\"timestamp\": \"2024-01-15T10:30:00+05:00\"}";

            TestDtoWithTime result = objectMapper.readValue(json, TestDtoWithTime.class);

            assertThat(result.timestamp.getOffset()).isEqualTo(ZoneOffset.ofHours(5));
        });
    }

    @Test
    @DisplayName("Jackson2ObjectMapperCustomizer should NOT be loaded when no pre-existing ObjectMapper exists")
    void shouldNotLoadJackson2ObjectMapperCustomizerBean() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean("objectMapperAutoConfiguration.Jackson2ObjectMapperCustomizer");
        });
    }

    static class TestDto {
        public String value;
    }

    static class TestDtoWithTime {
        public OffsetDateTime timestamp;
    }

    static class TestDtoWithInt {
        public Integer count;
    }
}

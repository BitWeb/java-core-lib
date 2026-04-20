package ee.bitweb.core.exception.validation;

import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class InvalidFormatValidationExceptionTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    @DisplayName("UNKNOWN_VALUE constant should be 'Unknown'")
    void unknownValueConstantShouldBeUnknown() {
        assertEquals("Unknown", InvalidFormatValidationException.UNKNOWN_VALUE);
    }

    @Test
    @DisplayName("Should extract field name from InvalidFormatException")
    void shouldExtractFieldNameFromInvalidFormatException() {
        String json = "{\"status\": \"INVALID_STATUS\"}";

        InvalidFormatException originalException = assertThrows(
                InvalidFormatException.class,
                () -> jsonMapper.readValue(json, TestDto.class)
        );

        InvalidFormatValidationException exception = new InvalidFormatValidationException(originalException);

        assertEquals("status", exception.getField());
    }

    @Test
    @DisplayName("Should extract value from InvalidFormatException")
    void shouldExtractValueFromInvalidFormatException() {
        String json = "{\"status\": \"INVALID_STATUS\"}";

        InvalidFormatException originalException = assertThrows(
                InvalidFormatException.class,
                () -> jsonMapper.readValue(json, TestDto.class)
        );

        InvalidFormatValidationException exception = new InvalidFormatValidationException(originalException);

        assertEquals("INVALID_STATUS", exception.getValue());
    }

    @Test
    @DisplayName("Should extract target class from InvalidFormatException")
    void shouldExtractTargetClassFromInvalidFormatException() {
        String json = "{\"status\": \"INVALID_STATUS\"}";

        InvalidFormatException originalException = assertThrows(
                InvalidFormatException.class,
                () -> jsonMapper.readValue(json, TestDto.class)
        );

        InvalidFormatValidationException exception = new InvalidFormatValidationException(originalException);

        assertEquals(Status.class, exception.getTargetClass());
    }

    @Test
    @DisplayName("Should handle nested field names")
    void shouldHandleNestedFieldNames() {
        String json = "{\"nested\": {\"status\": \"INVALID\"}}";

        InvalidFormatException originalException = assertThrows(
                InvalidFormatException.class,
                () -> jsonMapper.readValue(json, WrapperDto.class)
        );

        InvalidFormatValidationException exception = new InvalidFormatValidationException(originalException);

        assertEquals("nested.status", exception.getField());
    }

    @Test
    @DisplayName("Should set UNKNOWN_VALUE for MismatchedInputException")
    void shouldSetUnknownValueForMismatchedInputException() {
        String json = "{\"count\": \"not-a-number\"}";

        MismatchedInputException originalException = assertThrows(
                MismatchedInputException.class,
                () -> jsonMapper.readValue(json, IntegerDto.class)
        );

        InvalidFormatValidationException exception = new InvalidFormatValidationException(originalException);

        assertEquals(InvalidFormatValidationException.UNKNOWN_VALUE, exception.getValue());
    }

    @Test
    @DisplayName("Should extract field name from MismatchedInputException")
    void shouldExtractFieldNameFromMismatchedInputException() {
        String json = "{\"count\": \"not-a-number\"}";

        MismatchedInputException originalException = assertThrows(
                MismatchedInputException.class,
                () -> jsonMapper.readValue(json, IntegerDto.class)
        );

        InvalidFormatValidationException exception = new InvalidFormatValidationException(originalException);

        assertEquals("count", exception.getField());
    }

    @Test
    @DisplayName("Should extract target class from MismatchedInputException")
    void shouldExtractTargetClassFromMismatchedInputException() {
        String json = "{\"count\": \"not-a-number\"}";

        MismatchedInputException originalException = assertThrows(
                MismatchedInputException.class,
                () -> jsonMapper.readValue(json, IntegerDto.class)
        );

        InvalidFormatValidationException exception = new InvalidFormatValidationException(originalException);

        assertEquals(Integer.class, exception.getTargetClass());
    }

    @Test
    @DisplayName("Should be instance of RuntimeException")
    void shouldBeInstanceOfRuntimeException() {
        String json = "{\"status\": \"INVALID\"}";

        InvalidFormatException originalException = assertThrows(
                InvalidFormatException.class,
                () -> jsonMapper.readValue(json, TestDto.class)
        );

        InvalidFormatValidationException exception = new InvalidFormatValidationException(originalException);

        assertInstanceOf(RuntimeException.class, exception);
    }

    enum Status {
        ACTIVE, INACTIVE
    }

    static class TestDto {
        public Status status;
    }

    static class WrapperDto {
        public TestDto nested;
    }

    static class IntegerDto {
        public Integer count;
    }
}

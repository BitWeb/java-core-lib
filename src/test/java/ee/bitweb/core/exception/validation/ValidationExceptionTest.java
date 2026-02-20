package ee.bitweb.core.exception.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class ValidationExceptionTest {

    @Test
    @DisplayName("Should create with default message")
    void shouldCreateWithDefaultMessage() {
        Set<FieldError> errors = Set.of(
                new FieldError("email", "NotEmpty", "must not be empty")
        );

        ValidationException exception = new ValidationException(errors);

        assertEquals("Validation failed with errors", exception.getMessage());
    }

    @Test
    @DisplayName("Should create with custom message")
    void shouldCreateWithCustomMessage() {
        Set<FieldError> errors = Set.of(
                new FieldError("email", "NotEmpty", "must not be empty")
        );

        ValidationException exception = new ValidationException("Custom validation error", errors);

        assertEquals("Custom validation error", exception.getMessage());
    }

    @Test
    @DisplayName("Should store errors")
    void shouldStoreErrors() {
        FieldError error1 = new FieldError("email", "NotEmpty", "must not be empty");
        FieldError error2 = new FieldError("name", "Size", "must be between 1 and 100");
        Set<FieldError> errors = Set.of(error1, error2);

        ValidationException exception = new ValidationException(errors);

        assertEquals(2, exception.getErrors().size());
        assertTrue(exception.getErrors().contains(error1));
        assertTrue(exception.getErrors().contains(error2));
    }

    @Test
    @DisplayName("Should handle single error")
    void shouldHandleSingleError() {
        FieldError error = new FieldError("password", "Pattern", "must match pattern");
        Set<FieldError> errors = Set.of(error);

        ValidationException exception = new ValidationException(errors);

        assertEquals(1, exception.getErrors().size());
        assertTrue(exception.getErrors().contains(error));
    }

    @Test
    @DisplayName("Should handle empty errors set")
    void shouldHandleEmptyErrorsSet() {
        Set<FieldError> errors = Set.of();

        ValidationException exception = new ValidationException(errors);

        assertTrue(exception.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should be instance of CoreException")
    void shouldBeInstanceOfCoreException() {
        ValidationException exception = new ValidationException(Set.of());

        assertInstanceOf(ee.bitweb.core.exception.CoreException.class, exception);
    }
}

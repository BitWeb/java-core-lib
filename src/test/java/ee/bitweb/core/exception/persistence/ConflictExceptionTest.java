package ee.bitweb.core.exception.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class ConflictExceptionTest {

    @Test
    @DisplayName("Should return HTTP 409 status code")
    void shouldReturnConflictStatusCode() {
        ConflictException exception = new ConflictException("Conflict", "User", "email", "test@example.com");

        assertEquals(409, exception.getCode());
    }

    @Test
    @DisplayName("CODE constant should be 409")
    void codeConstantShouldBe409() {
        assertEquals(409, ConflictException.CODE);
    }

    @Test
    @DisplayName("Should create with message and single criteria")
    void shouldCreateWithMessageAndSingleCriteria() {
        ConflictException exception = new ConflictException(
                "Email already exists",
                "User",
                "email",
                "test@example.com"
        );

        assertAll(
                () -> assertEquals("Email already exists", exception.getMessage()),
                () -> assertEquals("User", exception.getEntity()),
                () -> assertEquals(1, exception.getCriteria().size())
        );

        Criteria criteria = exception.getCriteria().iterator().next();
        assertAll(
                () -> assertEquals("email", criteria.getField()),
                () -> assertEquals("test@example.com", criteria.getValue())
        );
    }

    @Test
    @DisplayName("Should create with message and multiple criteria")
    void shouldCreateWithMessageAndMultipleCriteria() {
        Set<Criteria> criteria = Set.of(
                new Criteria("email", "test@example.com"),
                new Criteria("username", "testuser")
        );

        ConflictException exception = new ConflictException(
                "User already exists",
                "User",
                criteria
        );

        assertAll(
                () -> assertEquals("User already exists", exception.getMessage()),
                () -> assertEquals("User", exception.getEntity()),
                () -> assertEquals(2, exception.getCriteria().size())
        );
    }

    @Test
    @DisplayName("Should generate default message when custom message is empty")
    void shouldGenerateDefaultMessageWhenEmpty() {
        Set<Criteria> criteria = Set.of(new Criteria("id", "123"));

        ConflictException exception = new ConflictException("", "User", criteria);

        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains("Exception with entity"));
    }

    @Test
    @DisplayName("Should generate default message when custom message is null")
    void shouldGenerateDefaultMessageWhenNull() {
        Set<Criteria> criteria = Set.of(new Criteria("id", "123"));

        ConflictException exception = new ConflictException(null, "User", criteria);

        assertTrue(exception.getMessage().contains("User"));
    }
}

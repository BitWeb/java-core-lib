package ee.bitweb.core.exception.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class EntityNotFoundExceptionTest {

    @Test
    @DisplayName("Should return HTTP 404 status code")
    void shouldReturnNotFoundStatusCode() {
        EntityNotFoundException exception = new EntityNotFoundException("User", "id", "123");

        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getCode());
    }

    @Test
    @DisplayName("Should create with custom message and single criteria")
    void shouldCreateWithCustomMessageAndSingleCriteria() {
        EntityNotFoundException exception = new EntityNotFoundException(
                "Custom message",
                "User",
                "id",
                "123"
        );

        assertAll(
                () -> assertEquals("Custom message", exception.getMessage()),
                () -> assertEquals("User", exception.getEntity()),
                () -> assertEquals(1, exception.getCriteria().size())
        );
    }

    @Test
    @DisplayName("Should create with custom message and multiple criteria")
    void shouldCreateWithCustomMessageAndMultipleCriteria() {
        Set<Criteria> criteria = Set.of(
                new Criteria("id", "123"),
                new Criteria("status", "ACTIVE")
        );

        EntityNotFoundException exception = new EntityNotFoundException(
                "Custom message",
                "User",
                criteria
        );

        assertAll(
                () -> assertEquals("Custom message", exception.getMessage()),
                () -> assertEquals("User", exception.getEntity()),
                () -> assertEquals(2, exception.getCriteria().size())
        );
    }

    @Test
    @DisplayName("Should generate default message when using entity and criteria")
    void shouldGenerateDefaultMessageWithEntityAndCriteria() {
        EntityNotFoundException exception = new EntityNotFoundException("User", "id", "123");

        assertEquals("Entity User not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should generate default message with multiple criteria")
    void shouldGenerateDefaultMessageWithMultipleCriteria() {
        Set<Criteria> criteria = Set.of(
                new Criteria("id", "123"),
                new Criteria("email", "test@example.com")
        );

        EntityNotFoundException exception = new EntityNotFoundException("User", criteria);

        assertEquals("Entity User not found", exception.getMessage());
    }

    static Stream<Arguments> criteriaTestCases() {
        return Stream.of(
                Arguments.of("User", "id", "123", "id", "123"),
                Arguments.of("Order", "orderId", "ABC", "orderId", "ABC"),
                Arguments.of("Product", "sku", "SKU-001", "sku", "SKU-001")
        );
    }

    @ParameterizedTest(name = "Entity {0} with field {1}={2}")
    @MethodSource("criteriaTestCases")
    void shouldStoreCriteriaCorrectly(String entity, String field, String value,
                                       String expectedField, String expectedValue) {
        EntityNotFoundException exception = new EntityNotFoundException(entity, field, value);

        Criteria criteria = exception.getCriteria().iterator().next();

        assertAll(
                () -> assertEquals(entity, exception.getEntity()),
                () -> assertEquals(expectedField, criteria.getField()),
                () -> assertEquals(expectedValue, criteria.getValue())
        );
    }
}

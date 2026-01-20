package ee.bitweb.core.api.model.exception;

import ee.bitweb.core.exception.persistence.Criteria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class CriteriaResponseTest {

    @Test
    @DisplayName("Should create from Criteria object")
    void shouldCreateFromCriteriaObject() {
        Criteria criteria = new Criteria("id", "123");

        CriteriaResponse response = new CriteriaResponse(criteria);

        assertAll(
                () -> assertEquals("id", response.getField()),
                () -> assertEquals("123", response.getValue())
        );
    }

    @Test
    @DisplayName("Should create with direct parameters")
    void shouldCreateWithDirectParameters() {
        CriteriaResponse response = new CriteriaResponse("email", "test@example.com");

        assertAll(
                () -> assertEquals("email", response.getField()),
                () -> assertEquals("test@example.com", response.getValue())
        );
    }

    static Stream<Arguments> comparisonCases() {
        return Stream.of(
                Arguments.of("alpha", "1", "beta", "1", -1, "field alpha < beta"),
                Arguments.of("beta", "1", "alpha", "1", 1, "field beta > alpha"),
                Arguments.of("field", "1", "field", "1", 0, "equal fields and values"),
                Arguments.of("field", "aaa", "field", "bbb", -1, "same field, value aaa < bbb"),
                Arguments.of("field", "bbb", "field", "aaa", 1, "same field, value bbb > aaa")
        );
    }

    @ParameterizedTest(name = "compareTo: {5}")
    @MethodSource("comparisonCases")
    void shouldCompareCorrectly(String field1, String value1, String field2, String value2,
                                 int expectedSign, String description) {
        CriteriaResponse first = new CriteriaResponse(field1, value1);
        CriteriaResponse second = new CriteriaResponse(field2, value2);

        int result = first.compareTo(second);

        assertEquals(expectedSign, Integer.signum(result));
    }

    @Test
    @DisplayName("Should compare case-insensitively")
    void shouldCompareCaseInsensitively() {
        CriteriaResponse lower = new CriteriaResponse("email", "value");
        CriteriaResponse upper = new CriteriaResponse("EMAIL", "VALUE");

        assertEquals(0, lower.compareTo(upper));
    }

    @Test
    @DisplayName("Should handle null field - nulls come first")
    void shouldHandleNullFieldNullsFirst() {
        CriteriaResponse withNull = new CriteriaResponse(null, "value");
        CriteriaResponse withValue = new CriteriaResponse("field", "value");

        assertTrue(withNull.compareTo(withValue) < 0);
        assertTrue(withValue.compareTo(withNull) > 0);
    }

    @Test
    @DisplayName("Should handle null value - nulls come first")
    void shouldHandleNullValueNullsFirst() {
        CriteriaResponse withNull = new CriteriaResponse("field", null);
        CriteriaResponse withValue = new CriteriaResponse("field", "value");

        assertTrue(withNull.compareTo(withValue) < 0);
        assertTrue(withValue.compareTo(withNull) > 0);
    }

    @Test
    @DisplayName("Should handle both null fields")
    void shouldHandleBothNullFields() {
        CriteriaResponse first = new CriteriaResponse(null, "a");
        CriteriaResponse second = new CriteriaResponse(null, "b");

        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    @DisplayName("Should handle comparison with null object")
    void shouldHandleComparisonWithNullObject() {
        CriteriaResponse response = new CriteriaResponse("field", "value");

        assertTrue(response.compareTo(null) > 0);
    }

    @Test
    @DisplayName("Should be equal when field and value match")
    void shouldBeEqualWhenFieldAndValueMatch() {
        CriteriaResponse first = new CriteriaResponse("field", "value");
        CriteriaResponse second = new CriteriaResponse("field", "value");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when field differs")
    void shouldNotBeEqualWhenFieldDiffers() {
        CriteriaResponse first = new CriteriaResponse("field1", "value");
        CriteriaResponse second = new CriteriaResponse("field2", "value");

        assertNotEquals(first, second);
    }

    @Test
    @DisplayName("Should not be equal when value differs")
    void shouldNotBeEqualWhenValueDiffers() {
        CriteriaResponse first = new CriteriaResponse("field", "value1");
        CriteriaResponse second = new CriteriaResponse("field", "value2");

        assertNotEquals(first, second);
    }
}

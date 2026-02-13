package ee.bitweb.core.object_mapper.deserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class TrimmedStringDeserializerTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        TrimmedStringDeserializer.addToObjectMapper(mapper);
    }

    static Stream<Arguments> stringTrimmingCases() {
        return Stream.of(
                Arguments.of("  hello", "hello", "leading whitespace"),
                Arguments.of("hello  ", "hello", "trailing whitespace"),
                Arguments.of("  hello world  ", "hello world", "both leading and trailing whitespace"),
                Arguments.of("hello   world", "hello   world", "internal whitespace preserved"),
                Arguments.of("   ", "", "whitespace-only string"),
                Arguments.of("", "", "empty string"),
                Arguments.of("\t\nhello\t\n", "hello", "tabs and newlines")
        );
    }

    @ParameterizedTest(name = "Should handle {2}")
    @MethodSource("stringTrimmingCases")
    void shouldTrimStrings(String input, String expected, String description) throws JsonProcessingException {
        String json = "\"" + escapeJson(input) + "\"";

        String result = mapper.readValue(json, String.class);

        assertEquals(expected, result, description);
    }

    @Test
    @DisplayName("Should return null for null value")
    void shouldReturnNullForNullValue() throws JsonProcessingException {
        String result = mapper.readValue("null", String.class);

        assertNull(result);
    }

    @Test
    @DisplayName("Should trim string fields in object")
    void shouldTrimStringFieldsInObject() throws JsonProcessingException {
        String json = "{\"name\": \"  John Doe  \", \"email\": \"  john@example.com  \"}";

        TestObject result = mapper.readValue(json, TestObject.class);

        assertAll(
                () -> assertEquals("John Doe", result.name),
                () -> assertEquals("john@example.com", result.email)
        );
    }

    @Test
    @DisplayName("Should trim strings in array")
    void shouldTrimStringsInArray() throws JsonProcessingException {
        String json = "[\"  first  \", \"  second  \", \"  third  \"]";

        String[] result = mapper.readValue(json, String[].class);

        assertAll(
                () -> assertEquals("first", result[0]),
                () -> assertEquals("second", result[1]),
                () -> assertEquals("third", result[2])
        );
    }

    private static String escapeJson(String input) {
        return input.replace("\t", "\\t").replace("\n", "\\n");
    }

    static class TestObject {
        public String name;
        public String email;
    }
}

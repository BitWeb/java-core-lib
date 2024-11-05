package ee.bitweb.core.retrofit.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class LoggingLevelTest {

    @Test
    @DisplayName("Ensure correct default mappers for level NONE")
    void testLevelNone() {
        assertTrue(LoggingLevel.NONE.getMappers().isEmpty());
    }

    @Test
    @DisplayName("Ensure correct default mappers for level CUSTOM")
    void testLevelCustom() {
        assertTrue(LoggingLevel.CUSTOM.getMappers().isEmpty());
    }

    @Test
    @DisplayName("Ensure correct default mappers for level BASIC")
    void testLevelBasic() {
        List<String> mappers = LoggingLevel.BASIC.getMappers();

        assertAll(
                () -> assertEquals(5, mappers.size()),
                () -> assertTrue(mappers.contains("request_method")),
                () -> assertTrue(mappers.contains("request_url")),
                () -> assertTrue(mappers.contains("request_body_size")),
                () -> assertTrue(mappers.contains("response_code")),
                () -> assertTrue(mappers.contains("response_body_size"))
        );
    }

    @Test
    @DisplayName("Ensure correct default mappers for level HEADERS")
    void testLevelHeaders() {
        List<String> mappers = LoggingLevel.HEADERS.getMappers();

        assertAll(
                () -> assertEquals(7, mappers.size()),
                () -> assertTrue(mappers.contains("request_method")),
                () -> assertTrue(mappers.contains("request_url")),
                () -> assertTrue(mappers.contains("request_body_size")),
                () -> assertTrue(mappers.contains("response_code")),
                () -> assertTrue(mappers.contains("response_body_size")),
                () -> assertTrue(mappers.contains("request_headers")),
                () -> assertTrue(mappers.contains("response_headers"))
        );
    }

    @Test
    @DisplayName("Ensure correct default mappers for level BODY")
    void testLevelBody() {
        List<String> mappers = LoggingLevel.BODY.getMappers();

        assertAll(
                () -> assertEquals(9, mappers.size()),
                () -> assertTrue(mappers.contains("request_method")),
                () -> assertTrue(mappers.contains("request_url")),
                () -> assertTrue(mappers.contains("request_body_size")),
                () -> assertTrue(mappers.contains("response_code")),
                () -> assertTrue(mappers.contains("response_body_size")),
                () -> assertTrue(mappers.contains("request_headers")),
                () -> assertTrue(mappers.contains("response_headers")),
                () -> assertTrue(mappers.contains("request_body")),
                () -> assertTrue(mappers.contains("response_body"))
        );
    }
}

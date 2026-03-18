package ee.bitweb.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@DisplayName("StringUtil")
class StringUtilTest {

    @Nested
    @DisplayName("trim()")
    class Trim {

        @Test
        @DisplayName("returns null for null input")
        void returnsNullForNullInput() {
            assertNull(StringUtil.trim(null));
        }

        @Test
        @DisplayName("returns empty string for empty input")
        void returnsEmptyStringForEmptyInput() {
            assertEquals("", StringUtil.trim(""));
        }

        @Test
        @DisplayName("removes leading whitespace")
        void removesLeadingWhitespace() {
            assertEquals("hello", StringUtil.trim("  hello"));
            assertEquals("hello", StringUtil.trim("\thello"));
            assertEquals("hello", StringUtil.trim("\nhello"));
        }

        @Test
        @DisplayName("removes trailing whitespace")
        void removesTrailingWhitespace() {
            assertEquals("hello", StringUtil.trim("hello  "));
            assertEquals("hello", StringUtil.trim("hello\t"));
            assertEquals("hello", StringUtil.trim("hello\n"));
        }

        @Test
        @DisplayName("removes both leading and trailing whitespace")
        void removesBothLeadingAndTrailingWhitespace() {
            assertEquals("hello", StringUtil.trim("  hello  "));
            assertEquals("hello world", StringUtil.trim("  hello world  "));
        }

        @Test
        @DisplayName("preserves internal whitespace")
        void preservesInternalWhitespace() {
            assertEquals("hello world", StringUtil.trim("hello world"));
            assertEquals("hello  world", StringUtil.trim("  hello  world  "));
        }

        @Test
        @DisplayName("returns original when no whitespace to trim")
        void returnsOriginalWhenNoWhitespace() {
            assertEquals("hello", StringUtil.trim("hello"));
        }
    }

    @Nested
    @DisplayName("random()")
    class Random {

        @Test
        @DisplayName("generates string of specified length")
        void generatesStringOfSpecifiedLength() {
            for (int i = 0; i < 10000; i++) {
                assertEquals(10, StringUtil.random(10).length());
                assertEquals(30, StringUtil.random(30).length());
            }
        }
    }
}

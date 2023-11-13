package ee.bitweb.core.util;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class StringUtilTest {

    @Test
    void random() {
        // test multiple times to avoid possible IndexOutOfBounds exceptions
        for (int i = 0; i < 10000; i++) {
            assertEquals(10, StringUtil.random(10).length());
            assertEquals(30, StringUtil.random(30).length());
        }
    }

}

package ee.bitweb.core.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UppercaseValidatorTest {

    private final UppercaseValidator validator = new UppercaseValidator();

    @Test
    @DisplayName("NULL string is valid")
    void testNullStringIsValid() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    @DisplayName("Empty string is valid")
    void testEmptyStringIsValid() {
        assertTrue(validator.isValid("", null));
    }

    @Test
    @DisplayName("Upper case string with numbers and symbols is valid")
    void testUpperCaseStringIsValid() {
        assertTrue(validator.isValid("TEST_STRING-_)987@", null));
    }

    @Test
    @DisplayName("String with only symbols is valid")
    void testStingWithOnlySymbolsIsValid() {
        assertTrue(validator.isValid("-_)987@.", null));
    }

    @Test
    @DisplayName("String with lower case char is invalid")
    void testLowerCaseStringIsInvalid() {
        assertFalse(validator.isValid("TEST_sTRING-_)987@", null));
    }
}

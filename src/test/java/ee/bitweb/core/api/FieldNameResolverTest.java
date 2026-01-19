package ee.bitweb.core.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.executable.ExecutableValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FieldNameResolverTest {

    private static ExecutableValidator executableValidator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        executableValidator = validator.forExecutables();
    }

    @Test
    @DisplayName("resolve() should return parameter name for @RequestParam validation errors")
    void resolveShouldReturnParameterNameForRequestParamValidation() throws NoSuchMethodException {
        // Simulate validation of a method parameter like @RequestParam @NotEmpty List<String> items
        Method method = TestController.class.getMethod("endpoint", List.class);
        Object[] parameterValues = { Collections.emptyList() };

        Set<ConstraintViolation<TestController>> violations = executableValidator.validateParameters(
                new TestController(),
                method,
                parameterValues
        );

        assertFalse(violations.isEmpty(), "Should have validation violations");

        ConstraintViolation<TestController> violation = violations.iterator().next();

        // The bug: FieldNameResolver.resolve() returns empty string for @RequestParam validation
        // because the path only contains METHOD and PARAMETER nodes, both of which are ignored
        String fieldName = FieldNameResolver.resolve(violation);

        // Expected: "items" (the parameter name)
        // Actual (bug): "" (empty string)
        assertEquals("items", fieldName, "Field name should be the parameter name 'items'");
    }

    @Test
    @DisplayName("resolveWithRegex() should return parameter name for @RequestParam validation errors")
    void resolveWithRegexShouldReturnParameterNameForRequestParamValidation() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("endpoint", List.class);
        Object[] parameterValues = { Collections.emptyList() };

        Set<ConstraintViolation<TestController>> violations = executableValidator.validateParameters(
                new TestController(),
                method,
                parameterValues
        );

        assertFalse(violations.isEmpty(), "Should have validation violations");

        ConstraintViolation<TestController> violation = violations.iterator().next();

        String fieldName = FieldNameResolver.resolveWithRegex(violation);

        assertEquals("items", fieldName, "Field name should be the parameter name 'items'");
    }

    /**
     * Test controller class to simulate @RequestParam validation scenario
     */
    static class TestController {

        public void endpoint(@NotEmpty List<String> items) {
            // Method simulating: @GetMapping public void endpoint(@RequestParam @NotEmpty List<String> items)
        }
    }
}

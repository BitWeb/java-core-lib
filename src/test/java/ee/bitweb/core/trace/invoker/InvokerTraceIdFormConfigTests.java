package ee.bitweb.core.trace.invoker;

import ee.bitweb.core.trace.TraceIdFormConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class InvokerTraceIdFormConfigTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validateDefaults() {
        TraceIdFormConfig config = new InvokerTraceIdFormConfig();

        assertEquals(20, config.getLength());
        assertEquals('_', config.getDelimiter());
        assertNull(config.getPrefix());

        assertEquals(0, validator.validate(config).size());
    }

    @Test
    void onTooSmallLengthShouldNotBeValid() {
        InvokerTraceIdFormConfig config = new InvokerTraceIdFormConfig();
        config.setLength(0);

        List<ConstraintViolation<InvokerTraceIdFormConfig>> errors = new ArrayList<>(validator.validate(config));

        assertEquals(1, validator.validate(config).size());
        assertEquals("must be greater than or equal to 10", errors.get(0).getMessage());
        assertEquals("length", getPropertyName(errors.get(0)));
        assertEquals("Min", getValidatorName(errors.get(0)));
    }

    @Test
    void onTooLargeLengthShouldNotBeValid() {
        InvokerTraceIdFormConfig config = new InvokerTraceIdFormConfig();
        config.setLength(100);

        List<ConstraintViolation<InvokerTraceIdFormConfig>> errors = new ArrayList<>(validator.validate(config));

        assertEquals(1, validator.validate(config).size());
        assertEquals("must be less than or equal to 20", errors.get(0).getMessage());
        assertEquals("length", getPropertyName(errors.get(0)));
        assertEquals("Max", getValidatorName(errors.get(0)));
    }

    @Test
    void onNullLengthShouldNotBeValid() {
        InvokerTraceIdFormConfig config = new InvokerTraceIdFormConfig();
        config.setLength(null);

        List<ConstraintViolation<InvokerTraceIdFormConfig>> errors = new ArrayList<>(validator.validate(config));

        assertEquals(1, validator.validate(config).size());
        assertEquals("must not be null", errors.get(0).getMessage());
        assertEquals("length", getPropertyName(errors.get(0)));
        assertEquals("NotNull", getValidatorName(errors.get(0)));
    }

    @Test
    void onPrefixLongerThanLengthShouldNotBeValid() {
        InvokerTraceIdFormConfig config = new InvokerTraceIdFormConfig();
        config.setLength(11);
        config.setPrefix("12345678901");

        List<ConstraintViolation<InvokerTraceIdFormConfig>> errors = new ArrayList<>(validator.validate(config));

        assertEquals(1, validator.validate(config).size());
        assertEquals("prefix cannot be longer than entire length of trace id", errors.get(0).getMessage());
        assertEquals("validLength", getPropertyName(errors.get(0)));
        assertEquals("AssertTrue", getValidatorName(errors.get(0)));
    }

    @Test
    void onNullDelimiterShouldNotBeValid() {
        InvokerTraceIdFormConfig config = new InvokerTraceIdFormConfig();
        config.setDelimiter(null);

        List<ConstraintViolation<InvokerTraceIdFormConfig>> errors = new ArrayList<>(validator.validate(config));

        assertEquals(1, validator.validate(config).size());
        assertEquals("must not be null", errors.get(0).getMessage());
        assertEquals("delimiter", getPropertyName(errors.get(0)));
        assertEquals("NotNull", getValidatorName(errors.get(0)));
    }

    private String getPropertyName(ConstraintViolation<?> error) {
        return error.getPropertyPath().toString();
    }

    private static String getValidatorName(ConstraintViolation<?> constraintViolation) {
        String validatorName = constraintViolation.getConstraintDescriptor().getAnnotation().annotationType().getName();
        String[] parts = validatorName.split("\\.");

        return parts[parts.length - 1];
    }
}

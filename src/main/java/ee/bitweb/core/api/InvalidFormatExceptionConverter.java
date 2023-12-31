package ee.bitweb.core.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import ee.bitweb.core.exception.validation.FieldError;
import ee.bitweb.core.exception.validation.InvalidFormatValidationException;
import ee.bitweb.core.exception.validation.ValidationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InvalidFormatExceptionConverter {

    public static final String INVALID_FORMAT_REASON = "InvalidFormat";
    public static final String INVALID_VALUE_REASON = "InvalidValue";

    public static final String INVALID_VALUE_MESSAGE_FORMAT = "Value not recognized (%s), please refer to specification for available values.";
    public static final String INVALID_BOOLEAN_VALUE_MESSAGE = "Value not recognized as boolean (%s), please refer to specification";
    public static final String INVALID_FLOAT_VALUE_MESSAGE = "Value not recognized as float (%s), please refer to specification";
    public static final String INVALID_INTEGER_VALUE_MESSAGE = "Value not recognized as integer (%s), please refer to specification";

    public static boolean canConvert(InvalidFormatValidationException e) {
        return StringUtils.hasText(e.getField()) && e.getValue() != null && e.getTargetClass() != null;
    }

    public static ValidationException convert(InvalidFormatValidationException e) {
        Set<FieldError> errors = new HashSet<>();

        addIfNotNull(errors, tryToCreateEnumErrorRow(e));
        addIfNotNull(errors, tryToCreateTemporalErrorRow(e));
        addIfNotNull(errors, tryToCreateNumericErrorRow(e));
        addIfNotNull(errors, tryToCreateBooleanErrorRow(e));

        if (errors.isEmpty()) {
            log.warn("Unrecognized Type: {}. Invalid format exception handled generically, for field {} and value {}."
                    + "Please consider adding type to given list", e.getTargetClass(), e.getField(), e.getValue());
            errors.add(new FieldError(
                    ErrorMessage.INVALID_ARGUMENT.toString(),
                    INVALID_VALUE_REASON,
                    String.format(INVALID_VALUE_MESSAGE_FORMAT, e.getValue())
            ));
        }

        return new ValidationException(ErrorMessage.INVALID_ARGUMENT.toString(), errors);
    }

    private static FieldError tryToCreateEnumErrorRow(InvalidFormatValidationException e) {
        if (!e.getTargetClass().isEnum()) {
            return null;
        }

        return new FieldError(
                e.getField(),
                INVALID_VALUE_REASON,
                String.format(INVALID_VALUE_MESSAGE_FORMAT, e.getValue())
        );
    }

    private static FieldError tryToCreateTemporalErrorRow(InvalidFormatValidationException e) {
        if (
            e.getTargetClass() == ZonedDateTime.class ||
            e.getTargetClass() == LocalDate.class ||
            e.getTargetClass() == LocalDateTime.class
        ) {
            return new FieldError(
                    e.getField(),
                    INVALID_FORMAT_REASON,
                    String.format(INVALID_VALUE_MESSAGE_FORMAT, e.getValue())
            );
        }

        return null;
    }

    private static FieldError tryToCreateNumericErrorRow(InvalidFormatValidationException e) {
        if (
                e.getTargetClass() == Long.class ||
                e.getTargetClass() == Integer.class ||
                e.getTargetClass() == Short.class ||
                e.getTargetClass() == int.class ||
                e.getTargetClass() == short.class ||
                e.getTargetClass() == long.class
        ) {

            return new FieldError(
                    e.getField(),
                    INVALID_FORMAT_REASON,
                    String.format(INVALID_INTEGER_VALUE_MESSAGE, e.getValue())
            );
        }

        if (
            e.getTargetClass() == Double.class ||
            e.getTargetClass() == BigDecimal.class ||
            e.getTargetClass() == double.class
        ) {
            return new FieldError(
                    e.getField(),
                    INVALID_FORMAT_REASON,
                    String.format(INVALID_FLOAT_VALUE_MESSAGE, e.getValue())
            );
        }

        return null;
    }

    private static FieldError tryToCreateBooleanErrorRow(InvalidFormatValidationException e) {
        if (e.getTargetClass() == Boolean.class) {
            return new FieldError(
                    e.getField(),
                    INVALID_VALUE_REASON,
                    String.format(INVALID_BOOLEAN_VALUE_MESSAGE, e.getValue())
            );
        }

        return null;
    }

    private static void addIfNotNull(Collection<FieldError> set, FieldError row) {
        if (row != null) {
            set.add(row);
        }
    }
}

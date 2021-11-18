package ee.bitweb.core.api;

import ee.bitweb.core.exception.validation.FieldError;
import ee.bitweb.core.exception.validation.ValidationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionConverter {

    public static final String CONSTRAINT_VIOLATION_MESSAGE = "CONSTRAINT_VIOLATION";

    public static ValidationException convert(ConstraintViolationException e) {
        Set<FieldError> fieldErrors = e
                .getConstraintViolations()
                .stream()
                .map(error -> new FieldError(
                        getFieldName(error),
                        getValidatorName(error),
                        parseMessage(error.getMessage())
                ))
                .collect(Collectors.toSet());

        return new ValidationException(CONSTRAINT_VIOLATION_MESSAGE, fieldErrors);
    }

    public static ValidationException translateBindingResult(BindingResult bindingResult) {
        Set<FieldError> fieldErrors = new HashSet<>();

        fieldErrors.addAll(bindingResult.getFieldErrors().stream().map(error -> new FieldError(
                error.getField(),
                error.getCodes() != null ? error.getCodes()[0].split("\\.")[0] : null,
                parseMessage(error.getDefaultMessage())
        )).collect(Collectors.toList()));

        fieldErrors.addAll(bindingResult.getGlobalErrors().stream().map(error -> new FieldError(
                error.getObjectName(),
                error.getCodes() != null ? error.getCodes()[0].split("\\.")[0] : null,
                parseMessage(error.getDefaultMessage())
        )).collect(Collectors.toList()));

        return new ValidationException(ErrorMessage.INVALID_ARGUMENT.toString(), fieldErrors);
    }

    private static String getFieldName(ConstraintViolation<?> error) {
        String[] parts = error.getPropertyPath().toString().split("\\.");

        return parts[parts.length - 1];
    }

    private static String getValidatorName(ConstraintViolation<?> constraintViolation) {
        String validatorName = constraintViolation.getConstraintDescriptor().getAnnotation().annotationType().getName();
        String[] parts = validatorName.split("\\.");

        return parts[parts.length - 1];
    }

    private static String parseMessage(String message) {
        if (message != null
                && (message.contains("java.util.Date") || message.contains("java.time.LocalDate"))
                && message.contains("java.lang.IllegalArgumentException")) {
            return message.substring(message.indexOf("java.lang.IllegalArgumentException") + 36);
        }

        return message;
    }
}

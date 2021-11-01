package ee.bitweb.core.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import ee.bitweb.core.exception.validation.FieldError;
import ee.bitweb.core.exception.validation.ValidationException;

import org.springframework.validation.BindingResult;

public class ExceptionConverter {

    public static String CONSTRAINT_VIOLATION_MESSAGE = "CONSTRAINT_VIOLATION";

    public static ValidationException convert(ConstraintViolationException e) {
        Set<FieldError> fieldErrors = new HashSet<>();

        fieldErrors.addAll(
                e.getConstraintViolations()
                        .stream()
                        .map(error -> new FieldError(
                                getFieldName(error),
                                getValidatorName(error),
                                parseMessage(error.getMessage())
                        ))
                        .collect(Collectors.toList())
        );

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

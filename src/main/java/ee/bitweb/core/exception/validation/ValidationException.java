package ee.bitweb.core.exception.validation;

import java.util.Set;
import ee.bitweb.core.exception.CoreException;

import lombok.Getter;

@Getter
public class ValidationException extends CoreException {

    private final Set<FieldError> errors;

    public ValidationException(String message, Set<FieldError> errors) {
        super(message);
        this.errors = errors;
    }

    public ValidationException(Set<FieldError> errors) {
        this(String.format("Validation failed with errors %s", errors), errors);
    }
}

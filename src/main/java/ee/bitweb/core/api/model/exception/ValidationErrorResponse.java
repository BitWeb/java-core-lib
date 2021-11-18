package ee.bitweb.core.api.model.exception;

import lombok.Getter;

import java.util.Collection;
import java.util.TreeSet;

import ee.bitweb.core.exception.validation.ValidationException;

@Getter
public class ValidationErrorResponse extends GenericErrorResponse {

    private final TreeSet<FieldErrorResponse> errors = new TreeSet<>(FieldErrorResponse::compareTo);

    public ValidationErrorResponse(String id, String message, Collection<FieldErrorResponse> errors) {
        super(id, message);
        this.errors.addAll(errors);
    }

    public ValidationErrorResponse(String id, ValidationException e) {
        super(id, e.getMessage());
    }
}

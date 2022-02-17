package ee.bitweb.core.api.model.exception;

import lombok.Getter;

import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ee.bitweb.core.exception.validation.ValidationException;

@Getter
public class ValidationErrorResponse extends GenericErrorResponse {

    private final TreeSet<FieldErrorResponse> errors = new TreeSet<>(FieldErrorResponse::compareTo);

    public ValidationErrorResponse(String id, String message, Collection<FieldErrorResponse> errors) {
        super(id, message);
        this.errors.addAll(errors);
    }

    public ValidationErrorResponse(String id, ValidationException e) {
        this(id, e.getMessage(), e.getErrors().stream().map(FieldErrorResponse::new).collect(Collectors.toList()));
    }
}

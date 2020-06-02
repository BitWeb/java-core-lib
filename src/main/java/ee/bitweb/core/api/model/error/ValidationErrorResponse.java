package ee.bitweb.core.api.model.error;

import ee.bitweb.core.api.ValidationErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.TreeSet;

@Getter
@RequiredArgsConstructor
public class ValidationErrorResponse {

    private final ValidationErrorType type;

    private final TreeSet<ValidationErrorRow> rows = new TreeSet<>(ValidationErrorRow::compareTo);
}

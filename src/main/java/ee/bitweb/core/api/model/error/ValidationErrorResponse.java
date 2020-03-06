package ee.bitweb.core.api.model.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public class ValidationErrorResponse {

    private final Set<ValidationErrorRow> rows;
}

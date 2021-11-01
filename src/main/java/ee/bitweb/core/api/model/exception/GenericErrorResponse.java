package ee.bitweb.core.api.model.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GenericErrorResponse {

    private final String id;
    private final String message;
}

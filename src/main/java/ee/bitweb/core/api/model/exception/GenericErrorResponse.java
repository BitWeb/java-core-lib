package ee.bitweb.core.api.model.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@RequiredArgsConstructor
public class GenericErrorResponse {

    private final String id;
    private final String message;
}

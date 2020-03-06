package ee.bitweb.core.api.model.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InternalErrorResponse {

    private final String id;
}

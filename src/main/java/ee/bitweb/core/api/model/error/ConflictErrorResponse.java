package ee.bitweb.core.api.model.error;

import ee.bitweb.core.exception.ConflictException;
import lombok.Getter;

@Getter
public class ConflictErrorResponse {

    private final String entity;
    private final String name;
    private final String value;

    public ConflictErrorResponse(ConflictException e) {
        entity = e.getEntity();
        name = e.getName();
        value = e.getValue();
    }
}

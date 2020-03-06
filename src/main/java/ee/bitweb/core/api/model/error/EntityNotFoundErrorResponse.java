package ee.bitweb.core.api.model.error;

import ee.bitweb.core.exception.EntityNotFoundException;
import lombok.Getter;

@Getter
public class EntityNotFoundErrorResponse {

    private final String entity;
    private final String field;
    private final String value;

    public EntityNotFoundErrorResponse(EntityNotFoundException e) {
        entity = e.getEntity();
        field = e.getField();
        value = e.getValue();
    }
}

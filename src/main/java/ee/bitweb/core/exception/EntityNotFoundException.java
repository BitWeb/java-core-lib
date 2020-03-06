package ee.bitweb.core.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends CoreException {

    private final String entity;
    private final String field;
    private final String value;

    public EntityNotFoundException(String entity, String field, String value) {
        super(String.format("Could not find entity %s where %s=%s", entity, field, value));

        this.entity = entity;
        this.field = field;
        this.value = value;
    }

    public EntityNotFoundException(String message, String entity) {
        super(message);

        this.entity = entity;
        this.field = null;
        this.value = null;
    }
}

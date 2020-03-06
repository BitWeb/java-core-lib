package ee.bitweb.core.exception;

import lombok.Getter;

@Getter
public class ConflictException extends CoreException {

    private final String entity;
    private final String name;
    private final String value;

    public ConflictException(String entity, String name, String value) {
        super(String.format("Entity %s with %s=%s already exists", entity, name, value));

        this.entity = entity;
        this.name = name;
        this.value = value;
    }
}

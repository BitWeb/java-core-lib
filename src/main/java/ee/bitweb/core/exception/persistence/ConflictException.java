package ee.bitweb.core.exception.persistence;

import java.util.Set;

import lombok.Getter;

@Getter
public class ConflictException extends PersistenceException {

    public static final int CODE = 409;

    public ConflictException(String message, String entity, String name, String value) {
        super(message, entity, name, value);
    }

    public ConflictException(String message, String entity, Set<Criteria> criteria) {
        super(message, entity, criteria);
    }

    @Override
    public int getCode() {
        return CODE;
    }
}

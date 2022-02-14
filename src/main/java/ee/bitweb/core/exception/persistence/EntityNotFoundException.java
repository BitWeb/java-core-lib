package ee.bitweb.core.exception.persistence;

import java.util.Set;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EntityNotFoundException extends PersistenceException {

    public EntityNotFoundException(String message, String entity, String field, String value) {
        this(message, entity, Set.of(new Criteria(field, value)));
    }

    public EntityNotFoundException(String message, String entity, Set<Criteria> criteria) {
        super(message, entity, criteria);
    }

    public EntityNotFoundException(String entity, Set<Criteria> criteria) {
        this(String.format("Entity %s not found", entity), entity, criteria);
    }

    public EntityNotFoundException(String entity, String field, String value) {
        this(entity, Set.of(new Criteria(field, value)));
    }

    @Override
    public int getCode() {
        return HttpStatus.NOT_FOUND.value();
    }
}

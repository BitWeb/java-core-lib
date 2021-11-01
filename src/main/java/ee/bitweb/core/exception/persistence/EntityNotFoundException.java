package ee.bitweb.core.exception.persistence;

import java.util.Set;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EntityNotFoundException extends PersistenceException {

    public EntityNotFoundException(String entity, Set<Criteria> criteria) {
        super(String.format("Entity %s not found", entity), entity, criteria);
    }

    @Override
    public int getCode() {
        return HttpStatus.NOT_FOUND.value();
    }
}

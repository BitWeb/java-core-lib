package ee.bitweb.core.exception.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ee.bitweb.core.exception.CoreException;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public abstract class PersistenceException extends CoreException {

    private final String entity;
    private final Set<Criteria> criteria;

    protected PersistenceException(String message, String entity, String field, String value) {
        this(message, entity, new HashSet<>(List.of(new Criteria(field, value))));
    }

    protected PersistenceException(String message, String entity, Set<Criteria> criteria) {
        super(StringUtils.isBlank(message) ? String.format("Exception with entity %s where %s", entity, criteria) : message);

        this.entity = entity;
        this.criteria = criteria;
    }

    public abstract int getCode();

}

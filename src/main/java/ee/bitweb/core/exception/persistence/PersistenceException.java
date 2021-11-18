package ee.bitweb.core.exception.persistence;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ee.bitweb.core.exception.CoreException;

import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
public abstract class PersistenceException extends CoreException {

    private final String entity;
    private final Set<Criteria> criteria; // todo: SonarLint: Make "criteria" transient or serializable.

    protected PersistenceException(String message, String entity, String field, String value) {
        this(message, entity, new HashSet<>(List.of(new Criteria(field, value))));
    }

    protected PersistenceException(String message, String entity, Set<Criteria> criteria) {
        super(StringUtils.hasText(message) ? message : String.format("Exception with entity %s where %s", entity, criteria));

        this.entity = entity;
        this.criteria = criteria;
    }

    public abstract int getCode();
}

package ee.bitweb.core.api.model.exception;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ee.bitweb.core.exception.persistence.Criteria;
import ee.bitweb.core.exception.persistence.PersistenceException;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class PersistenceErrorResponse extends GenericErrorResponse {

    private final String entity;
    private final TreeSet<CriteriaResponse> criteria = new TreeSet<>(CriteriaResponse::compareTo);


    public PersistenceErrorResponse(String id, String message, String entity, Set<Criteria> criteria) {
        super(id, message);
        this.entity = entity;
        this.criteria.addAll(criteria.stream().map(CriteriaResponse::new).collect(Collectors.toList()));
    }

    public PersistenceErrorResponse(String id, PersistenceException e) {
        this(id, e.getMessage(), e.getEntity(), e.getCriteria());
    }

}

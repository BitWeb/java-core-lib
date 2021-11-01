package ee.bitweb.core.api.model.exception;

import java.util.Comparator;

import ee.bitweb.core.exception.persistence.Criteria;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class CriteriaResponse implements Comparable<CriteriaResponse> {

    private static final Comparator<String> NULL_SAFE_STRING_COMPARATOR = Comparator.nullsFirst(
            String::compareToIgnoreCase
    );

    private final String field;

    private final String value;

    public CriteriaResponse(Criteria criteria) {
        this.field = criteria.getField();
        this.value = criteria.getValue();
    }

    @Override
    public int compareTo(CriteriaResponse o) {
        return Comparator.nullsFirst(
                Comparator.comparing(CriteriaResponse::getField, NULL_SAFE_STRING_COMPARATOR)
                        .thenComparing(CriteriaResponse::getValue, NULL_SAFE_STRING_COMPARATOR)
        ).compare(this, o);
    }
}

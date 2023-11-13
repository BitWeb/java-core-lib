package ee.bitweb.core.api.model.exception;

import java.util.Comparator;

import ee.bitweb.core.exception.validation.FieldError;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class FieldErrorResponse implements Comparable<FieldErrorResponse> {

    private static final Comparator<String> NULL_SAFE_STRING_COMPARATOR = Comparator.nullsFirst(
            String::compareToIgnoreCase
    );

    private final String field;

    private final String reason;

    private final String message;

    public FieldErrorResponse(FieldError e) {
        this(e.getField(), e.getReason(), e.getMessage());
    }

    @Override
    public int compareTo(FieldErrorResponse o) {
        return Comparator.nullsFirst(
                Comparator.comparing(FieldErrorResponse::getField, NULL_SAFE_STRING_COMPARATOR)
                        .thenComparing(FieldErrorResponse::getReason, NULL_SAFE_STRING_COMPARATOR)
                        .thenComparing(FieldErrorResponse::getMessage, NULL_SAFE_STRING_COMPARATOR)
        ).compare(this, o);
    }
}

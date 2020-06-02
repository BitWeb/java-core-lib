package ee.bitweb.core.api.model.error;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Comparator;

@ToString
@Getter
@RequiredArgsConstructor
public class ValidationErrorRow implements Comparable<ValidationErrorRow> {

    private static final Comparator<String> NULL_SAFE_STRING_COMPARATOR = Comparator.nullsFirst(
            String::compareToIgnoreCase
    );

    @ApiModelProperty("Field name that was not correct")
    private final String field;

    @ApiModelProperty("Name of the validator that failed")
    private final String reason;

    @ApiModelProperty("Human readable message of what was wrong")
    private final String message;

    @Override
    public int compareTo(ValidationErrorRow o) {
        return Comparator.nullsFirst(
                Comparator.comparing(ValidationErrorRow::getField, NULL_SAFE_STRING_COMPARATOR)
                        .thenComparing(ValidationErrorRow::getReason, NULL_SAFE_STRING_COMPARATOR)
                        .thenComparing(ValidationErrorRow::getMessage, NULL_SAFE_STRING_COMPARATOR)
        ).compare(this, o);
    }
}

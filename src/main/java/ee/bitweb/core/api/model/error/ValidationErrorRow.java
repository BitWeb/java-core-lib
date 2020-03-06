package ee.bitweb.core.api.model.error;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@RequiredArgsConstructor
public class ValidationErrorRow {

    @ApiModelProperty("Field name that was not correct")
    private final String field;

    @ApiModelProperty("Name of the validator that failed")
    private final String reason;

    @ApiModelProperty("Human readable message of what was wrong")
    private final String message;
}

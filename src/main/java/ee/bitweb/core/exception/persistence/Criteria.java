package ee.bitweb.core.exception.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class Criteria {

    private final String field;
    private final String value;
}

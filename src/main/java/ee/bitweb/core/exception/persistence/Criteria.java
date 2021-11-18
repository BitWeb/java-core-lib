package ee.bitweb.core.exception.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
@AllArgsConstructor
public class Criteria implements Serializable {

    private final String field;
    private final String value;
}

package ee.bitweb.core.amqp.testcomponents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    private String traceId;
    private String postProcessMessageParameter;
}

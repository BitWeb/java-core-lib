package ee.bitweb.core.trace.invoker.amqp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Validated
@ConfigurationProperties("ee.bitweb.core.trace.invoker.amqp")
public class AmqpTraceProperties {

    public static final String DEFAULT_HEADER_NAME = "x-trace-id";

    @NotBlank
    private String headerName = DEFAULT_HEADER_NAME;
}

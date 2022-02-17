package ee.bitweb.core.trace.invoker;

import ee.bitweb.core.trace.TraceIdFormConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.*;

@Setter
@Getter
@Validated
@ConfigurationProperties("ee.bitweb.core.trace.invoker")
public class InvokerTraceIdFormConfig implements TraceIdFormConfig {

    public static final Integer MAX_LENGTH = 20;
    public static final Integer MIN_LENGTH = 10;
    public static final Character DEFAULT_DELIMITER = '_';

    private String prefix;

    @NotNull
    private Character delimiter = DEFAULT_DELIMITER;

    @Min(10)
    @Max(20)
    @NotNull
    private Integer length = MAX_LENGTH;

    @AssertTrue(message = "prefix cannot be longer than entire length of trace id")
    public boolean isValidLength() {
        return prefix == null || prefix.length() < length;
    }
}

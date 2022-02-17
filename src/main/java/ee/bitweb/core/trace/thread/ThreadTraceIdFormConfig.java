package ee.bitweb.core.trace.thread;

import ee.bitweb.core.trace.TraceIdFormConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Setter
@Getter
@Validated
@ConfigurationProperties("ee.bitweb.core.trace.thread")
public class ThreadTraceIdFormConfig implements TraceIdFormConfig {

    public static final Integer MAX_LENGTH = 10;
    public static final Integer MIN_LENGTH = 5;
    public static final Character DEFAULT_DELIMITER = ':';

    private String prefix;

    @NotNull
    private Character delimiter = DEFAULT_DELIMITER;

    @Min(5)
    @Max(10)
    @NotNull
    private Integer length = MAX_LENGTH;

    @AssertTrue(message = "prefix cannot be longer than entire length of trace id")
    public boolean isValidLength() {
        return prefix == null || prefix.length() < length;
    }
}

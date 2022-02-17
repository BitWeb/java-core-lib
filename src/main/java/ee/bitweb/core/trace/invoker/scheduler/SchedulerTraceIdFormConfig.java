package ee.bitweb.core.trace.invoker.scheduler;

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
@ConfigurationProperties("ee.bitweb.core.trace.scheduler")
public class SchedulerTraceIdFormConfig implements TraceIdFormConfig {

    public static final int MAX_LENGTH = 20;
    public static final int MIN_LENGTH = 10;

    @Min(MIN_LENGTH)
    @Max(MAX_LENGTH)
    @NotNull
    private Integer length = MAX_LENGTH;

    private String prefix;

    @Override
    public Character getDelimiter() {
        throw new IllegalStateException("Scheduler is a root invoker, thus prefix should never be used");
    }

    @AssertTrue(message = "prefix cannot be longer than entire length of trace id")
    public boolean isValidLength() {
        return prefix == null || prefix.length() < length;
    }
}

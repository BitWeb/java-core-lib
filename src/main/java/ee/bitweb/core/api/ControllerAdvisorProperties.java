package ee.bitweb.core.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import static ee.bitweb.core.api.ControllerAdvisorProperties.PREFIX;

@Setter
@Getter
@Validated
@Component
@ConfigurationProperties(PREFIX)
@ConditionalOnProperty(value = PREFIX + ".auto-configuration", havingValue = "true")
public class ControllerAdvisorProperties {

    static final String PREFIX = "ee.bitweb.core.controller-advice";

    private boolean autoConfiguration = false;

    @Valid
    private Logging logging = new Logging();

    @Getter
    @Setter
    @Validated
    public static class Logging {

        @NotNull
        private Level bindException = Level.INFO;

        @NotNull
        private Level constraintViolationException = Level.INFO;

        @NotNull
        private Level httpMediaTypeNotSupportedException = Level.WARN;

        @NotNull
        private Level httpRequestMethodNotSupportedException = Level.WARN;

        @NotNull
        private Level httpMessageNotReadableException = Level.WARN;

        @NotNull
        private Level methodArgumentNotValidException = Level.INFO;

        @NotNull
        private Level methodArgumentTypeMismatchException = Level.INFO;

        @NotNull
        private Level missingServletRequestParameterException = Level.INFO;

        @NotNull
        private Level missingServletRequestPartException = Level.INFO;

        @NotNull
        private Level multipartException = Level.WARN;

        @NotNull
        private Level persistenceException = Level.ERROR;

        @NotNull
        private Level retrofitException = Level.INFO;

        @NotNull
        private Level clientAbortException = Level.WARN;
    }

    public enum Level {
        OFF, TRACE, DEBUG, INFO, WARN, ERROR
    }
}

package ee.bitweb.core.retrofit;

import ee.bitweb.core.retrofit.builder.LoggingLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static ee.bitweb.core.retrofit.RetrofitProperties.PREFIX;

@Component
@Setter
@Getter
@Validated
@ConfigurationProperties(PREFIX)
@ConditionalOnProperty(value = PREFIX + ".auto-configuration", havingValue = "true")
public class RetrofitProperties {

    static final String PREFIX = "ee.bitweb.core.retrofit";

    private boolean autoConfiguration = false;

    @Valid
    private Logging logging = new Logging();

    @Valid
    private AuthTokenInjector authTokenInjector = new AuthTokenInjector();

    @Valid
    private Timeout timeout = new Timeout();

    @Getter
    @Setter
    @Validated
    public static class Logging {

        @NotNull
        private LoggingLevel level = LoggingLevel.BASIC;
        private List<@NotBlank String> suppressedHeaders = new ArrayList<>();
    }

    @Getter
    @Setter
    @Validated
    public static class AuthTokenInjector {

        private boolean autoConfiguration;

        private String headerName = HttpHeaders.AUTHORIZATION;

        private List<@NotBlank String> whitelistUrls = new ArrayList<>();

        @AssertTrue(message = "header name must be non blank value")
        public boolean assertHeaderNameValid() {
            return !autoConfiguration || StringUtils.hasText(headerName);
        }
    }

    @Getter
    @Setter
    @Validated
    public static class Timeout {

        private Long call = 0L;

        private Long connect = 10_000L;

        private Long read = 10_000L;

        private Long write = 10_000L;
    }
}

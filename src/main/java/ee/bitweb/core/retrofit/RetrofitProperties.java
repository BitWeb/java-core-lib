package ee.bitweb.core.retrofit;

import ee.bitweb.core.retrofit.builder.LoggingLevel;
import ee.bitweb.core.trace.invoker.http.TraceIdFilterConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


@Component
@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "ee.bitweb.core.retrofit")
@ConditionalOnProperty(value = "ee.bitweb.core.retrofit.auto-configuration", havingValue = "true")
public class RetrofitProperties {

    private boolean autoConfiguration;

    @Valid
    private Logging logging = new Logging();

    @Valid
    private AuthTokenInjector authTokenInjector = new AuthTokenInjector();

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

        private boolean enabled;

        private String headerName = TraceIdFilterConfig.DEFAULT_HEADER_NAME;

        private List<@NotBlank String> whitelistUrls = new ArrayList<>();

        @AssertTrue(message = "header name must be non blank value")
        public boolean assertHeaderNameValid() {
            return !enabled || StringUtils.hasText(headerName);
        }
    }
}

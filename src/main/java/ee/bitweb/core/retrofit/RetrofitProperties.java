package ee.bitweb.core.retrofit;

import ee.bitweb.core.retrofit.builder.LoggingLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static ee.bitweb.core.retrofit.RetrofitProperties.PREFIX;

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

        @NotNull
        private Long maxLoggableRequestBodySize = 1024 * 10L;

        @NotNull
        private Long maxLoggableResponseBodySize = 1024 * 10L;

        private List<@NotBlank String> suppressedHeaders = new ArrayList<>();
        private List<@NotBlank String> redactedBodyUrls = new ArrayList<>();

        private List<@NotBlank String> mappers = new ArrayList<>();

        public List<@NotBlank String> getMappers() {
            if (level == LoggingLevel.CUSTOM) {
                return mappers;
            } else {
                Set<String> enabledMappers = new HashSet<>(level.getMappers());
                enabledMappers.addAll(mappers);

                return enabledMappers.stream().toList();
            }
        }
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

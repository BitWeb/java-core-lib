package ee.bitweb.core.audit;

import ee.bitweb.core.audit.mappers.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ee.bitweb.core.audit.AuditLogProperties.PREFIX;

@Getter
@Setter
@Validated
@ConfigurationProperties(PREFIX)
@ConditionalOnProperty(value = PREFIX + ".auto-configuration", havingValue = "true")
public class AuditLogProperties {

    static final String PREFIX = "ee.bitweb.core.audit";

    private boolean autoConfiguration = false;

    @Positive
    private long maxLoggableResponseSize = 1024 * 3L;

    @Positive
    private long maxLoggableRequestSize = 1024 * 3L;

    @NotNull
    private List<@NotBlank String> requestHeaders = new ArrayList<>(List.of("User-Agent", "Origin"));

    @NotNull
    private List<@NotBlank String> responseHeaders = new ArrayList<>();

    @NotNull
    private List<@NotBlank String> sensitiveHeaders = new ArrayList<>(List.of("authorization"));

    @NotNull
    private List<@NotBlank String> mappers = new ArrayList<>(
            Arrays.asList(
                    RequestForwardingDataMapper.KEY,
                    RequestHeadersMapper.KEY,
                    RequestMethodMapper.KEY,
                    RequestUrlDataMapper.KEY,
                    RequestBodyMapper.KEY,
                    ResponseBodyMapper.KEY,
                    ResponseStatusMapper.KEY,
                    TraceIdMapper.KEY
            )
    );

    @NotNull
    private List<String> blacklist = new ArrayList<>(List.of("/actuator/"));

    private boolean includeDuration = true;
    private boolean includeDefaultMappers = true;
}

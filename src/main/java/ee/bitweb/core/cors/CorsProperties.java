package ee.bitweb.core.cors;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

import static ee.bitweb.core.cors.CorsProperties.PREFIX;

@Setter
@Getter
@Validated
@ConfigurationProperties(PREFIX)
public class CorsProperties {

    static final String PREFIX = "ee.bitweb.core.cors";

    private boolean autoConfiguration = false;

    @NotBlank
    private String path = "/**";

    private boolean allowCredentials = true;

    @NotEmpty
    private List<@NotBlank String> allowedOrigins = new ArrayList<>();

    private List<@NotBlank String> allowedMethods = List.of(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name(),
            HttpMethod.PATCH.name()
    );
}

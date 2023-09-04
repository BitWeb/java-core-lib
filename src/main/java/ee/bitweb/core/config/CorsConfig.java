package ee.bitweb.core.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "ee.bitweb.cors")
@ConditionalOnProperty(value = "ee.bitweb.core.cors.auto-configuration.enabled", havingValue = "true")
public class CorsConfig {

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


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Creating CorsConfigurationSource");
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowCredentials(allowCredentials);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(path, configuration);

        return source;
    }

}

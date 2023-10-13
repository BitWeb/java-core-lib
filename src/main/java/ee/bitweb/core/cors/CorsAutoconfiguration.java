package ee.bitweb.core.cors;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({CorsProperties.class})
@ConditionalOnProperty(value = CorsProperties.PREFIX + ".auto-configuration", havingValue = "true")
public class CorsAutoconfiguration implements WebMvcConfigurer {

    private final CorsProperties properties;

    @PostConstruct
    public void init() {
        log.info("Cors autoconfiguration enabled");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping(properties.getPath())
                .allowCredentials(properties.isAllowCredentials())
                .allowedMethods(properties.getAllowedMethods().toArray(new String[0]))
                .allowedOrigins(properties.getAllowedOrigins().toArray(new String[0]));
    }
}

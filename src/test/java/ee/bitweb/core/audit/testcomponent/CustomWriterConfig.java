package ee.bitweb.core.audit.testcomponent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("CustomAuditLogWriter")
public class CustomWriterConfig {

    @Bean
    public CustomAuditLogWriter customAuditLogWriter() {
        return new CustomAuditLogWriter();
    }
}

package ee.bitweb.core.audit;

import ee.bitweb.core.audit.mappers.AbstractAuditLogDataMapper;
import ee.bitweb.core.audit.writers.AuditLogLoggerWriterAdapter;
import ee.bitweb.core.audit.writers.AuditLogWriteAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "ee.bitweb.core.audit.auto-configuration", havingValue="true")
@EnableConfigurationProperties({AuditLogProperties.class})
public class AuditLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuditLogFilter auditLogFilter(
            AuditLogProperties properties,
            List<AbstractAuditLogDataMapper> mappers,
            AuditLogWriteAdapter writer
    ) {
        log.info("Registering Audit Log Filter with writer {}", writer.getClass());

        for (AbstractAuditLogDataMapper mapper : mappers) {
            log.info("Applying Audit Log Data Mapper: {}", mapper.getClass());
        }

        return new AuditLogFilter(properties, mappers, writer);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditLogWriteAdapter auditLogWriteAdapter() {
        return new AuditLogLoggerWriterAdapter();
    }
}

package ee.bitweb.core.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.audit.mappers.*;
import ee.bitweb.core.audit.writers.AuditLogLoggerWriterAdapter;
import ee.bitweb.core.audit.writers.AuditLogWriteAdapter;
import ee.bitweb.core.trace.context.TraceIdContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
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
            List<AuditLogDataMapper> mappers,
            AuditLogWriteAdapter writer
    ) {
        log.info("Registering Audit Log Filter with writer {}", writer.getClass());

        for (AuditLogDataMapper mapper : mappers) {
            log.info("Applying Audit Log Data Mapper: {}", mapper.getClass());
        }

        return new AuditLogFilter(properties, mappers, writer);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditLogWriteAdapter auditLogWriteAdapter() {
        return new AuditLogLoggerWriterAdapter();
    }


    @Bean
    @ConditionalOnEnabledMapper(mapper = RequestForwardingDataMapper.KEY)
    public RequestForwardingDataMapper requestForwardingDataMapper(
            AuditLogProperties properties,
            ObjectMapper mapper
    ) {
        return new RequestForwardingDataMapper(properties, mapper);
    }

    @Bean
    @ConditionalOnEnabledMapper(mapper = RequestHeadersMapper.KEY)
    public RequestHeadersMapper requestHeadersMapper(
            AuditLogProperties properties,
            ObjectMapper mapper
    ) {
        return new RequestHeadersMapper(properties, mapper);
    }

    @Bean
    @ConditionalOnEnabledMapper(mapper = RequestMethodMapper.KEY)
    public RequestMethodMapper requestMethodMapper() {
        return new RequestMethodMapper();
    }


    @Bean
    @ConditionalOnEnabledMapper(mapper = RequestUrlDataMapper.KEY)
    public RequestUrlDataMapper requestUrlDataMapper() {
        return new RequestUrlDataMapper();
    }

    @Bean
    @ConditionalOnEnabledMapper(mapper = ResponseBodyMapper.KEY)
    public ResponseBodyMapper responseBodyMapper(AuditLogProperties properties) {
        return new ResponseBodyMapper(properties);
    }

    @Bean
    @ConditionalOnEnabledMapper(mapper = ResponseStatusMapper.KEY)
    public ResponseStatusMapper responseStatusMapper() {
        return new ResponseStatusMapper();
    }

    @Bean
    @ConditionalOnEnabledMapper(mapper = RequestBodyMapper.KEY)
    public RequestBodyMapper requestBodyMapper() {
        return new RequestBodyMapper();
    }

    @Bean
    @Conditional(TraceIdMapperEligible.class)
    public TraceIdMapper traceIdMapper(TraceIdContext context) {
        return new TraceIdMapper(context);
    }

    static class TraceIdMapperEligible extends AnyNestedCondition {
        TraceIdMapperEligible () { super(ConfigurationPhase.REGISTER_BEAN);}

        @ConditionalOnBean(TraceIdContext.class)
        static class TraceIdContextExists {}

        @ConditionalOnEnabledMapper(mapper = TraceIdMapper.KEY)
        static class TraceIdMapperEnabled {}
    }
}

package ee.bitweb.core.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.audit.mappers.*;
import ee.bitweb.core.exception.CoreException;
import ee.bitweb.core.trace.context.TraceIdContext;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

@Configuration
@ConditionalOnBean(AuditLogProperties.class)
public class AuditLogDefaultMapperLoader {

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Conditional(value = MapperEnabled.class)
    public @interface ConditionalOnEnabledMapper {

        String mapper();
    }

    static class MapperEnabled implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Map<String, Object> attributes= metadata.getAnnotationAttributes(
                    ConditionalOnEnabledMapper.class.getName()
            );
            if (attributes == null) return false;

            Object mapperKeyOb = attributes.get("mapper");

            if (mapperKeyOb == null) return false;

            String mapperKey = (String) mapperKeyOb;

            AuditLogProperties config = Binder.get(
                    context.getEnvironment()
            ).bind(
                    AuditLogProperties.PREFIX, AuditLogProperties.class
            ).orElseThrow(
                    () -> new CoreException("Error occured while trying to bind environment to AuditLogProperties")
            );

            return config.getMappers().contains(mapperKey);
        }
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

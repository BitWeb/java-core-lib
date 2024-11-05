package ee.bitweb.core.retrofit;

import ee.bitweb.core.exception.CoreException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(value = ConditionalOnEnabledRetrofitMapper.MapperEnabled.class)
public @interface ConditionalOnEnabledRetrofitMapper {

    String mapper();

    class MapperEnabled implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Map<String, Object> attributes = metadata.getAnnotationAttributes(
                    ConditionalOnEnabledRetrofitMapper.class.getName()
            );
            if (attributes == null) return false;

            Object mapperKeyOb = attributes.get("mapper");

            if (mapperKeyOb == null) return false;

            String mapperKey = (String) mapperKeyOb;

            RetrofitProperties config = Binder.get(
                    context.getEnvironment()
            ).bind(
                    RetrofitProperties.PREFIX, RetrofitProperties.class
            ).orElseThrow(
                    () -> new CoreException("Error occurred while trying to bind environment to RetrofitProperties")
            );

            return config.getLogging().getMappers().contains(mapperKey);
        }
    }
}

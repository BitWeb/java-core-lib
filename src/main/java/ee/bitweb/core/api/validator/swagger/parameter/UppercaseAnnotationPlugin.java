package ee.bitweb.core.api.validator.swagger.parameter;

import com.google.common.base.Optional;
import ee.bitweb.core.api.validator.Uppercase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.bean.validators.plugins.Validators;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ParameterBuilderPlugin;
import springfox.documentation.spi.service.contexts.ParameterContext;

import static ee.bitweb.core.api.validator.swagger.SwaggerPluginHelper.getPrefix;
import static springfox.bean.validators.plugins.Validators.annotationFromParameter;

@Slf4j
@Component
@Order(Validators.BEAN_VALIDATOR_PLUGIN_ORDER)
public class UppercaseAnnotationPlugin implements ParameterBuilderPlugin {

    @Override
    public boolean supports(DocumentationType delimiter) {
        // we simply support all documentationTypes!
        return true;
    }

    @Override
    public void apply(ParameterContext context) {
        Optional<Uppercase> size = annotationFromParameter(context, Uppercase.class);

        if (size.isPresent()) {
            Parameter parameter = context.parameterBuilder().build();
            String prefix = getPrefix(parameter.getDescription(), parameter.getName());

            context.parameterBuilder().description(prefix + " Must be uppercase");
        }
    }
}

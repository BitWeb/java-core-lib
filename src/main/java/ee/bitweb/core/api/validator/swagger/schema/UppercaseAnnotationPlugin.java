package ee.bitweb.core.api.validator.swagger.schema;

import com.google.common.base.Optional;
import ee.bitweb.core.api.validator.Uppercase;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.bean.validators.plugins.Validators;
import springfox.documentation.schema.ModelProperty;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.ModelPropertyBuilderPlugin;
import springfox.documentation.spi.schema.contexts.ModelPropertyContext;

import static ee.bitweb.core.api.validator.swagger.SwaggerPluginHelper.getPrefix;
import static springfox.bean.validators.plugins.Validators.annotationFromBean;

@Component("schemaUppercaseAnnotationPlugin")
@Order(Validators.BEAN_VALIDATOR_PLUGIN_ORDER)
public class UppercaseAnnotationPlugin implements ModelPropertyBuilderPlugin {

    @Override
    public void apply(ModelPropertyContext context) {
        Optional<Uppercase> uppercase = annotationFromBean(context, Uppercase.class);

        if (uppercase.isPresent()) {
            ModelProperty build = context.getBuilder().build();
            String prefix = getPrefix(build.getDescription(), build.getName());

            context.getBuilder().description(prefix + " Must be uppercase");
        }
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}

package ee.bitweb.core.config;

import com.fasterxml.classmate.TypeResolver;
import ee.bitweb.core.api.model.exception.GenericErrorResponse;
import ee.bitweb.core.api.model.exception.PersistenceErrorResponse;
import ee.bitweb.core.api.model.exception.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractSwaggerConfig {

    public Docket createDocket(TypeResolver typeResolver, String basePackage) {
        List<ResponseMessage> responseMessages = responseMessages();

        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .paths(PathSelectors.any())
                .build()
                .additionalModels(typeResolver.resolve(GenericErrorResponse.class))
                .additionalModels(typeResolver.resolve(PersistenceErrorResponse.class))
                .globalResponseMessage(RequestMethod.GET, responseMessages)
                .globalResponseMessage(RequestMethod.POST, responseMessages)
                .globalResponseMessage(RequestMethod.PUT, responseMessages)
                .globalResponseMessage(RequestMethod.PATCH, responseMessages)
                .globalResponseMessage(RequestMethod.DELETE, responseMessages)
                .consumes(Set.of(MediaType.APPLICATION_JSON_VALUE))
                .produces(Set.of(MediaType.APPLICATION_JSON_VALUE))
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false);
    }

    protected abstract ApiInfo apiInfo();

    protected CorsFilter createCorsFilter() {
        var source = new UrlBasedCorsConfigurationSource();

        // Allow anyone and anything access api documentation
        var config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/api-docs", config);

        return new CorsFilter(source);
    }

    protected List<ResponseMessage> responseMessages() {
        List<ResponseMessage> messages = new ArrayList<>();

        messages.add(new ResponseMessageBuilder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Validation error")
                .responseModel(new ModelRef(ValidationErrorResponse.class.getSimpleName()))
                .build());

        messages.add(new ResponseMessageBuilder()
                .code(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                .message("Internal error")
                .responseModel(new ModelRef(GenericErrorResponse.class.getSimpleName()))
                .build());

        messages.add(new ResponseMessageBuilder()
                .code(HttpServletResponse.SC_NOT_FOUND)
                .message("Entity not found")
                .responseModel(new ModelRef(PersistenceErrorResponse.class.getSimpleName()))
                .build());

        return messages;
    }
}

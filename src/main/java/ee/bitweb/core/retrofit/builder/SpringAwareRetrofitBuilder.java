package ee.bitweb.core.retrofit.builder;

import ee.bitweb.core.retrofit.RetrofitProperties;
import ee.bitweb.core.retrofit.interceptor.InterceptorBean;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import retrofit2.Converter;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "ee.bitweb.core.retrofit.auto-configuration", havingValue = "true")
public class SpringAwareRetrofitBuilder {

    private final List<InterceptorBean> defaultInterceptors;
    private final Converter.Factory defaultConverterFactory;
    private final RetrofitProperties properties;

    public <T> RetrofitApiBuilder<T> create(String baseUrl, Class<T> definition) {

        return RetrofitApiBuilder.create(baseUrl, definition)
                .addAll(new ArrayList<>(defaultInterceptors))
                .loggingLevel(properties.getLogging().getLevel())
                .suppressedHeaders(properties.getLogging().getSuppressedHeaders())
                .converter(defaultConverterFactory);
    }
}

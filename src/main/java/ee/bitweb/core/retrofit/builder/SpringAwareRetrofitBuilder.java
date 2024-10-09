package ee.bitweb.core.retrofit.builder;

import ee.bitweb.core.retrofit.RetrofitProperties;
import ee.bitweb.core.retrofit.interceptor.InterceptorBean;
import ee.bitweb.core.retrofit.logging.LoggingInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import retrofit2.Converter;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "ee.bitweb.core.retrofit.auto-configuration", havingValue = "true")
public class SpringAwareRetrofitBuilder {

    private final List<InterceptorBean> defaultInterceptors;
    private final Converter.Factory defaultConverterFactory;
    private final RetrofitProperties properties;

    public <T> RetrofitApiBuilder<T> create(String baseUrl, Class<T> definition) {
        return configure(RetrofitApiBuilder.create(baseUrl, definition));
    }

    public <T> RetrofitApiBuilder<T> create(String baseUrl, Class<T> definition, LoggingInterceptor loggingInterceptor) {
        return configure(RetrofitApiBuilder.create(baseUrl, definition, loggingInterceptor));
    }

    private <T> RetrofitApiBuilder<T> configure(RetrofitApiBuilder<T> api) {
        return api.addAll(new ArrayList<>(defaultInterceptors))
                .loggingLevel(properties.getLogging().getLevel())
                .suppressedHeaders(properties.getLogging().getSuppressedHeaders())
                .callTimeout(properties.getTimeout().getCall())
                .connectTimeout(properties.getTimeout().getConnect())
                .readTimeout(properties.getTimeout().getRead())
                .writeTimeout(properties.getTimeout().getWrite())
                .converter(defaultConverterFactory);
    }
}

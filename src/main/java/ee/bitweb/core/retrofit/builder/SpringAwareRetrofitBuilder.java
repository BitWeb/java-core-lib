package ee.bitweb.core.retrofit.builder;

import ee.bitweb.core.retrofit.RetrofitProperties;
import ee.bitweb.core.retrofit.interceptor.InterceptorBean;
import ee.bitweb.core.retrofit.logging.RetrofitLoggingInterceptor;
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
    private final RetrofitLoggingInterceptor defaultLoggingInterceptor;

    public <T> RetrofitApiBuilder<T> create(String baseUrl, Class<T> definition) {
        return configure(RetrofitApiBuilder.create(baseUrl, definition, defaultLoggingInterceptor));
    }

    public <T> RetrofitApiBuilder<T> create(String baseUrl, Class<T> definition, RetrofitLoggingInterceptor loggingInterceptor) {
        return configure(RetrofitApiBuilder.create(baseUrl, definition, loggingInterceptor));
    }

    private <T> RetrofitApiBuilder<T> configure(RetrofitApiBuilder<T> api) {
        return api.addAll(new ArrayList<>(defaultInterceptors))
                .callTimeout(properties.getTimeout().getCall())
                .connectTimeout(properties.getTimeout().getConnect())
                .readTimeout(properties.getTimeout().getRead())
                .writeTimeout(properties.getTimeout().getWrite())
                .converter(defaultConverterFactory);
    }
}

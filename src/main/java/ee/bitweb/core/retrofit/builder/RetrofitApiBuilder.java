package ee.bitweb.core.retrofit.builder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ee.bitweb.core.retrofit.logging.LoggingInterceptor;
import ee.bitweb.core.retrofit.logging.LoggingLevel;
import ee.bitweb.core.retrofit.logging.RetrofitLoggingInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RetrofitApiBuilder<T> {

    public static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();
    public static final LoggingLevel DEFAULT_LOGGING_LEVEL = LoggingLevel.BASIC;

    static {
        DEFAULT_OBJECT_MAPPER.registerModule(new JavaTimeModule())
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
    }

    private static final JacksonConverterFactory DEFAULT_CONVERTER_FACTORY = JacksonConverterFactory.create(DEFAULT_OBJECT_MAPPER);

    private final String url;
    private final Class<T> definition;
    private final LoggingInterceptor loggingInterceptor;

    private Converter.Factory converterFactory;
    private OkHttpClient.Builder clientBuilder;

    public static <T> RetrofitApiBuilder<T> create(String baseUrl, Class<T> definition) {
        return new RetrofitApiBuilder<>(
                baseUrl,
                definition,
                new RetrofitLoggingInterceptor()
                        .setLevel(DEFAULT_LOGGING_LEVEL)
        );
    }

    public static <T> RetrofitApiBuilder<T> create(String baseUrl, Class<T> definition, LoggingInterceptor loggingInterceptor) {
        return new RetrofitApiBuilder<>(
                baseUrl,
                definition,
                loggingInterceptor
        );
    }

    private RetrofitApiBuilder(String url, Class<T> definition, LoggingInterceptor loggingInterceptor) {
        this.url = url;
        this.definition = definition;
        this.loggingInterceptor = loggingInterceptor;

        clientBuilder = createDefaultBuilder(loggingInterceptor);
    }

    public RetrofitApiBuilder<T> emptyInterceptors() {
        clientBuilder.interceptors().clear();

        return this;
    }

    public RetrofitApiBuilder<T> add(Interceptor interceptor) {
        clientBuilder.interceptors().add(interceptor);

        return this;
    }

    public RetrofitApiBuilder<T> remove(Interceptor interceptor) {
        clientBuilder.interceptors().remove(interceptor);

        return this;
    }

    public RetrofitApiBuilder<T> removeAll(Class<? extends Interceptor> definition) {
        List<Interceptor> candidates = new ArrayList<>();

        for (Interceptor c : clientBuilder.interceptors()) {
            if (c.getClass() == definition) {
                candidates.add(c);
            }
        }

        for (Interceptor c : candidates) {
            this.remove(c);
        }

        return this;
    }

    public RetrofitApiBuilder<T> replaceAllOfType(Interceptor interceptor) {
        removeAll(interceptor.getClass());
        add(interceptor);

        return this;
    }

    public RetrofitApiBuilder<T> loggingLevel(LoggingLevel level) {
        loggingInterceptor.setLevel(level);

        return this;
    }

    public RetrofitApiBuilder<T> suppressedHeaders(List<String> headers) {
        headers.forEach(loggingInterceptor::redactHeader);

        return this;
    }

    public RetrofitApiBuilder<T> addAll(Collection<Interceptor> interceptors) {
        clientBuilder.interceptors().addAll(interceptors);

        return this;
    }

    public RetrofitApiBuilder<T> converter(Converter.Factory converter) {
        this.converterFactory = converter;

        return this;
    }

    public RetrofitApiBuilder<T> clientBuilder(OkHttpClient.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;

        return this;
    }

    public RetrofitApiBuilder<T> callTimeout(long timeout) {
        clientBuilder.callTimeout(timeout, TimeUnit.MILLISECONDS);

        return this;
    }

    public RetrofitApiBuilder<T> connectTimeout(long timeout) {
        clientBuilder.connectTimeout(timeout, TimeUnit.MILLISECONDS);

        return this;
    }

    public RetrofitApiBuilder<T> readTimeout(long timeout) {
        clientBuilder.readTimeout(timeout, TimeUnit.MILLISECONDS);

        return this;
    }

    public RetrofitApiBuilder<T> writeTimeout(long timeout) {
        clientBuilder.writeTimeout(timeout, TimeUnit.MILLISECONDS);

        return this;
    }

    public T build() {
        Converter.Factory factory = converterFactory != null ? converterFactory : DEFAULT_CONVERTER_FACTORY;

        log.info(
                "Built Retrofit API for host {} with definition {}, interceptors {} and converter factory {}",
                url, definition.getName(), clientBuilder.interceptors(), factory
        );

        return new Retrofit
                .Builder()
                .baseUrl(url)
                .addConverterFactory(factory)
                .client(clientBuilder.build())
                .build().create(definition);
    }

    private OkHttpClient.Builder createDefaultBuilder(Interceptor loggingInterceptor) {
        var httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.interceptors().add(loggingInterceptor);

        return httpClientBuilder;
    }
}

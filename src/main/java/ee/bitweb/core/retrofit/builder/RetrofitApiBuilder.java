package ee.bitweb.core.retrofit.builder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RetrofitApiBuilder<T> {

    public static final HttpLoggingInterceptor DEFAULT_LOGGING_INTERCEPTOR = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BASIC);

    public static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final JacksonConverterFactory DEFAULT_CONVERTER_FACTORY = JacksonConverterFactory.create(DEFAULT_OBJECT_MAPPER);

    private static final List<Interceptor> STANDARD_INTERCEPTORS = List.of(
            DEFAULT_LOGGING_INTERCEPTOR
    );

    private final String url;
    private final Class<T> definition;

    private Converter.Factory converterFactory;
    private OkHttpClient.Builder clientBuilder = createDefaultBuilder();

    public static <T> RetrofitApiBuilder<T>  create(String baseUrl, Class<T> definition) {
        return new RetrofitApiBuilder<>(
                baseUrl,
                definition
        );
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

    public T build() {
        return new Retrofit
                .Builder()
                .baseUrl(url)
                .addConverterFactory(converterFactory != null ? converterFactory : DEFAULT_CONVERTER_FACTORY)
                .client(clientBuilder.build())
                .build().create(definition);
    }

    private OkHttpClient.Builder createDefaultBuilder() {
        var httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.interceptors().addAll(STANDARD_INTERCEPTORS);

        return httpClientBuilder;
    }
}

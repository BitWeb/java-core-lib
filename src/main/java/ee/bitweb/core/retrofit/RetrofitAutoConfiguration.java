package ee.bitweb.core.retrofit;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.retrofit.interceptor.auth.AuthTokenInjectInterceptor;
import ee.bitweb.core.retrofit.interceptor.auth.TokenProvider;
import ee.bitweb.core.retrofit.interceptor.auth.criteria.AuthTokenCriteria;
import ee.bitweb.core.retrofit.interceptor.auth.criteria.WhitelistCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "ee.bitweb.core.retrofit.auto-configuration", havingValue = "true")
public class RetrofitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Converter.Factory.class)
    public Converter.Factory defaultJacksonConverterFactory(ObjectMapper mapper) {
        return JacksonConverterFactory.create(mapper);
    }

    @Bean
    @ConditionalOnProperty(value = "ee.bitweb.core.retrofit.authTokenInjector.enabled", havingValue = "true")
    public AuthTokenInjectInterceptor defaultAuthTokenInjectInterceptor(
            RetrofitProperties properties,
            TokenProvider provider,
            AuthTokenCriteria criteria
    ) {
        return new AuthTokenInjectInterceptor(
                properties.getAuthTokenInjector().getHeaderName(),
                provider,
                criteria
        );
    }

    @Bean
    @ConditionalOnProperty(value = "ee.bitweb.core.retrofit.authTokenInjector.enabled", havingValue = "true")
    public AuthTokenCriteria defaultCriteria(RetrofitProperties properties) {
        return new WhitelistCriteria(properties.getAuthTokenInjector().getWhitelistUrls());
    }
}

package ee.bitweb.core.retrofit;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.retrofit.interceptor.auth.AuthTokenInjectInterceptor;
import ee.bitweb.core.retrofit.interceptor.auth.TokenProvider;
import ee.bitweb.core.retrofit.interceptor.auth.criteria.AuthTokenCriteria;
import ee.bitweb.core.retrofit.interceptor.auth.criteria.WhitelistCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = RetrofitProperties.PREFIX + ".auto-configuration", havingValue = "true")
public class RetrofitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Converter.Factory.class)
    public Converter.Factory defaultJacksonConverterFactory(ObjectMapper mapper) {
        log.info("Creating default retrofit Jackson Converter with ObjectMapper bean");

        return JacksonConverterFactory.create(mapper);
    }

    @Bean
    @ConditionalOnProperty(value = RetrofitProperties.PREFIX + ".auth-token-injector.auto-configuration", havingValue = "true")
    public AuthTokenInjectInterceptor defaultAuthTokenInjectInterceptor(
            RetrofitProperties properties,
            TokenProvider provider,
            AuthTokenCriteria criteria
    ) {
        log.info("Creating Auth token Injection Interceptor for Retrofit");

        return new AuthTokenInjectInterceptor(
                properties.getAuthTokenInjector().getHeaderName(),
                provider,
                criteria
        );
    }

    @Bean
    @ConditionalOnProperty(value = RetrofitProperties.PREFIX + ".auth-token-injector.auto-configuration", havingValue = "true")
    public AuthTokenCriteria defaultCriteria(RetrofitProperties properties) {
        log.info("Creating Whitelist criteria for Retrofit auth token injection interceptor with patterns {}", properties.getAuthTokenInjector().getWhitelistUrls());

        List<Pattern> patterns = new ArrayList<>();

        for (String entry : properties.getAuthTokenInjector().getWhitelistUrls()) {
            patterns.add(Pattern.compile(entry));
        }

        WhitelistCriteria criteria = new WhitelistCriteria(patterns);
        criteria.validate();

        return criteria;
    }
}

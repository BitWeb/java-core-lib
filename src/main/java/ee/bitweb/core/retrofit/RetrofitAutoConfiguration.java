package ee.bitweb.core.retrofit;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.bitweb.core.retrofit.interceptor.auth.AuthTokenInjectInterceptor;
import ee.bitweb.core.retrofit.interceptor.auth.TokenProvider;
import ee.bitweb.core.retrofit.interceptor.auth.criteria.AuthTokenCriteria;
import ee.bitweb.core.retrofit.interceptor.auth.criteria.WhitelistCriteria;
import ee.bitweb.core.retrofit.logging.RetrofitLoggingInterceptor;
import ee.bitweb.core.retrofit.logging.RetrofitLoggingInterceptorImplementation;
import ee.bitweb.core.retrofit.logging.mappers.*;
import ee.bitweb.core.retrofit.logging.writers.RetrofitLogLoggerWriterAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = RetrofitProperties.PREFIX + ".auto-configuration", havingValue = "true")
@EnableConfigurationProperties({RetrofitProperties.class})
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

    @Bean("defaultRetrofitLoggingInterceptor")
    @Primary
    public RetrofitLoggingInterceptor defaultRetrofitLoggingInterceptor(
            List<RetrofitLoggingMapper> mappers
    ) {
        log.info(
                "Create Default Retrofit Logging Interceptor with writer {}",
                RetrofitLogLoggerWriterAdapter.class.getSimpleName()
        );

        for (RetrofitLoggingMapper mapper : mappers) {
            log.info("Applying Retrofit Log Data Mapper: {}", mapper.getClass());
        }

        return new RetrofitLoggingInterceptorImplementation(mappers, new RetrofitLogLoggerWriterAdapter());
    }

    @Bean
    @ConditionalOnEnabledRetrofitMapper(mapper = RetrofitRequestMethodMapper.KEY)
    public RetrofitRequestMethodMapper retrofitRequestMethodMapper() {
        return new RetrofitRequestMethodMapper();
    }

    @Bean
    @ConditionalOnEnabledRetrofitMapper(mapper = RetrofitRequestProtocolMapper.KEY)
    public RetrofitRequestProtocolMapper retrofitRequestProtocolMapper() {
        return new RetrofitRequestProtocolMapper();
    }

    @Bean
    @ConditionalOnEnabledRetrofitMapper(mapper = RetrofitRequestUrlMapper.KEY)
    public RetrofitRequestUrlMapper retrofitRequestUrlMapper() {
        return new RetrofitRequestUrlMapper();
    }

    @Bean
    @ConditionalOnEnabledRetrofitMapper(mapper = RetrofitRequestHeadersMapper.KEY)
    public RetrofitRequestHeadersMapper retrofitRequestHeadersMapper(
            RetrofitProperties retrofitProperties
    ) {
        return new RetrofitRequestHeadersMapper(new HashSet<>(retrofitProperties.getLogging().getSuppressedHeaders()));
    }

    @Bean
    @ConditionalOnEnabledRetrofitMapper(mapper = RetrofitRequestBodySizeMapper.KEY)
    public RetrofitRequestBodySizeMapper retrofitRequestBodySizeMapper() {
        return new RetrofitRequestBodySizeMapper();
    }

    @Bean
    @ConditionalOnEnabledRetrofitMapper(mapper = RetrofitRequestBodyMapper.KEY)
    public RetrofitRequestBodyMapper retrofitRequestBodyMapper(
            RetrofitProperties retrofitProperties
    ) {
        return new RetrofitRequestBodyMapper(
                retrofitProperties.getLogging().getMaxLoggableRequestBodySize().intValue(),
                new HashSet<>(retrofitProperties.getLogging().getRedactedBodyUrls())
        );
    }

    @Bean
    @ConditionalOnEnabledRetrofitMapper(mapper = RetrofitResponseStatusCodeMapper.KEY)
    public RetrofitResponseStatusCodeMapper retrofitResponseStatusCodeMapper() {
        return new RetrofitResponseStatusCodeMapper();
    }

    @Bean
    @ConditionalOnEnabledRetrofitMapper(mapper = RetrofitResponseMessageMapper.KEY)
    public RetrofitResponseMessageMapper retrofitResponseMessageMapper() {
        return new RetrofitResponseMessageMapper();
    }

    @Bean
    @ConditionalOnEnabledRetrofitMapper(mapper = RetrofitResponseHeadersMapper.KEY)
    public RetrofitResponseHeadersMapper retrofitResponseHeadersMapper(
            RetrofitProperties retrofitProperties
    ) {
        return new RetrofitResponseHeadersMapper(new HashSet<>(retrofitProperties.getLogging().getSuppressedHeaders()));
    }

    @Bean
    @ConditionalOnEnabledRetrofitMapper(mapper = RetrofitResponseBodySizeMapper.KEY)
    public RetrofitResponseBodySizeMapper retrofitResponseBodySizeMapper() {
        return new RetrofitResponseBodySizeMapper();
    }

    @Bean
    @ConditionalOnEnabledRetrofitMapper(mapper = RetrofitResponseBodyMapper.KEY)
    public RetrofitResponseBodyMapper responseBodyMapper(
            RetrofitProperties retrofitProperties
    ) {
        return new RetrofitResponseBodyMapper(
                new HashSet<>(retrofitProperties.getLogging().getRedactedBodyUrls()),
                retrofitProperties.getLogging().getMaxLoggableResponseBodySize().intValue()
        );
    }
}

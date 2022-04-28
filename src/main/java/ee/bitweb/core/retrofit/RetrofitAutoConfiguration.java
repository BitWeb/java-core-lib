package ee.bitweb.core.retrofit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
@ConditionalOnProperty(value = "ee.bitweb.core.retrofit.auto-configuration", havingValue = "true")
public class RetrofitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Converter.Factory.class)
    public Converter.Factory defaultJacksonConverterFactory(ObjectMapper mapper) {
        return JacksonConverterFactory.create(mapper);
    }
}

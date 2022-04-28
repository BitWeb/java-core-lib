package ee.bitweb.core.retrofit.helpers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@ActiveProfiles("retrofit")
public class RetrofitConfiguration {

    @Bean("interceptor1")
    public RequestCountInterceptor interceptor1() {
        return new RequestCountInterceptor();
    }

    @Bean("interceptor2")
    public RequestCountInterceptor interceptor2() {
        return new RequestCountInterceptor();
    }
}

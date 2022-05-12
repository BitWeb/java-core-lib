package ee.bitweb.core.retrofit;

import ee.bitweb.core.TestSpringApplication;
import ee.bitweb.core.retrofit.interceptor.TraceIdInterceptor;
import ee.bitweb.core.retrofit.interceptor.auth.AuthTokenInjectInterceptor;
import ee.bitweb.core.retrofit.interceptor.auth.criteria.AuthTokenCriteria;
import ee.bitweb.core.retrofit.interceptor.auth.criteria.WhitelistCriteria;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@SpringBootTest(
        classes= TestSpringApplication.class,
        properties = {
                "ee.bitweb.core.trace.auto-configuration=true",
                "ee.bitweb.core.retrofit.auto-configuration=true",
                "ee.bitweb.core.retrofit.auth-token-injector.enabled=true",
                "ee.bitweb.core.retrofit.auth-token-injector.headerName=some-header-name",
                "ee.bitweb.core.retrofit.auth-token-injector.whitelist-urls[0]=^http?:\\\\/\\\\/localhost:\\\\d{3,5}\\\\/.*"
        }
)
class RetrofitAutoConfigurationTests {

    @Autowired
    private Converter.Factory factory;

    @Autowired
    private TraceIdInterceptor traceIdInterceptor;

    @Autowired
    private AuthTokenCriteria whiteListCriteria;

    @Autowired
    private AuthTokenInjectInterceptor interceptor;

    @Test
    void onEnabledAutoConfigurationItWorksAsIntended() {
        assertAll(
                () -> assertEquals(JacksonConverterFactory.class, factory.getClass()),
                () -> assertNotNull(traceIdInterceptor),
                () -> assertEquals(WhitelistCriteria.class, whiteListCriteria.getClass()),
                () -> assertEquals("some-header-name", interceptor.getHeader()),
                () -> assertEquals(whiteListCriteria, interceptor.getCriteria()),
                () -> assertEquals(Pattern.compile("^http?:\\/\\/localhost:\\d{3,5}\\/.*").toString(), ((WhitelistCriteria)whiteListCriteria).getWhitelist().get(0).toString())
        );
    }
}

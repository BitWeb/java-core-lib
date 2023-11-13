package ee.bitweb.core.retrofit.interceptor.auth;

import ee.bitweb.core.retrofit.interceptor.InterceptorBean;
import ee.bitweb.core.retrofit.interceptor.auth.criteria.AuthTokenCriteria;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Getter
@ToString
@RequiredArgsConstructor
public class AuthTokenInjectInterceptor implements InterceptorBean {

    private final String header;
    private final TokenProvider provider;
    private final AuthTokenCriteria criteria;

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        if (!StringUtils.hasText(provider.get()) || !criteria.shouldApply(provider, chain)) {
            return chain.proceed(chain.request());
        }

        var request = chain
                .request()
                .newBuilder()
                .addHeader(header, provider.get())
                .build();

        return chain.proceed(request);
    }
}

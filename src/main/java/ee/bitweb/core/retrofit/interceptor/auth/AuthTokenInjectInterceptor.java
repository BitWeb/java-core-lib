package ee.bitweb.core.retrofit.interceptor.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class AuthTokenInjectInterceptor implements Interceptor {

    private final String header;
    private final TokenProvider provider;

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        if (!StringUtils.hasText(provider.get())) {
            return chain.proceed(chain.request());
        }

        Request request = chain
                .request()
                .newBuilder()
                .addHeader(header, provider.get())
                .build();

        return chain.proceed(request);
    }
}

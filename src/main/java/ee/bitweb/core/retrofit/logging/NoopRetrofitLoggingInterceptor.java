package ee.bitweb.core.retrofit.logging;

import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class NoopRetrofitLoggingInterceptor implements RetrofitLoggingInterceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        return chain.proceed(chain.request());
    }
}

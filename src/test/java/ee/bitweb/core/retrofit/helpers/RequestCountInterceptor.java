package ee.bitweb.core.retrofit.helpers;

import ee.bitweb.core.retrofit.interceptor.InterceptorBean;
import lombok.Getter;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
public class RequestCountInterceptor implements InterceptorBean {

    private Integer count = 0;

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        count++;

        return chain.proceed(chain.request());
    }

    public void reset() {
        count = 0;
    }
}

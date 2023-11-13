package ee.bitweb.core.retrofit.interceptor;

import ee.bitweb.core.trace.context.TraceIdContext;
import ee.bitweb.core.trace.invoker.http.TraceIdFilterConfig;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@ConditionalOnExpression(value = "${ee.bitweb.core.trace.auto-configuration:false} and ${ee.bitweb.core.retrofit.auto-configuration:false}")
public class TraceIdInterceptor implements InterceptorBean {

    private final TraceIdFilterConfig config;
    private final TraceIdContext context;

    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        assertTraceIdPresent();

        var request = chain
                .request()
                .newBuilder()
                .addHeader(config.getHeaderName(), context.get())
                .build();

        return chain.proceed(request);
    }

    private void assertTraceIdPresent() {
        if (!StringUtils.hasText(context.get())) {
            throw new IllegalStateException("Cannot execute Retrofit request without trace id present");
        }
    }
}

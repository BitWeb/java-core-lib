package ee.bitweb.core.retrofit.logging;

import ee.bitweb.core.retrofit.logging.mappers.RetrofitLoggingMapper;
import ee.bitweb.core.retrofit.logging.writers.RetrofitLogWriteAdapter;
import lombok.RequiredArgsConstructor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class RetrofitLoggingInterceptorImplementation implements RetrofitLoggingInterceptor {

    public static final String DURATION_KEY = "Duration";

    private final List<RetrofitLoggingMapper> mappers;
    private final RetrofitLogWriteAdapter writer;

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Map<String, String> container = new HashMap<>();

        var start = System.currentTimeMillis();

        Response response = null;
        try {
            response = chain.proceed(chain.request());
            container.put(DURATION_KEY, String.valueOf(System.currentTimeMillis() - start));
        } finally {
            Response finalResponse = response;

            Request request = finalResponse != null ? finalResponse.request() : chain.request();
            mappers.forEach(mapper -> mapper.map(chain.connection(), request, finalResponse, container));

            writer.write(container);
        }

        return response;
    }
}

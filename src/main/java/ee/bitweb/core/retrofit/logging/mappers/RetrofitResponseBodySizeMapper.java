package ee.bitweb.core.retrofit.logging.mappers;

import lombok.RequiredArgsConstructor;
import okhttp3.Connection;
import okhttp3.Request;
import okhttp3.Response;

@RequiredArgsConstructor
public class RetrofitResponseBodySizeMapper implements RetrofitLoggingMapper {

    public static final String KEY = "ResponseBodySize";

    @Override
    public String getValue(Connection connection, Request request, Response response) {
        return response.body() != null ? String.valueOf(response.body().contentLength()) : "-";
    }

    @Override
    public String getKey() {
        return KEY;
    }
}

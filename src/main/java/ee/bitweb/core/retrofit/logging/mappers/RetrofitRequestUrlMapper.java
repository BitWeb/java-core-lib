package ee.bitweb.core.retrofit.logging.mappers;

import lombok.RequiredArgsConstructor;
import okhttp3.Connection;
import okhttp3.Request;
import okhttp3.Response;

@RequiredArgsConstructor
public class RetrofitRequestUrlMapper implements RetrofitLoggingMapper {

    public static final String KEY = "RequestUrl";

    @Override
    public String getValue(Connection connection, Request request, Response response) {
        return request.url().toString();
    }

    @Override
    public String getKey() {
        return KEY;
    }
}

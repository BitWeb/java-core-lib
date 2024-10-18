package ee.bitweb.core.retrofit.logging.mappers;

import lombok.RequiredArgsConstructor;
import okhttp3.Connection;
import okhttp3.Request;
import okhttp3.Response;

@RequiredArgsConstructor
public class RetrofitResponseMessageMapper implements RetrofitLoggingMapper {

    public static final String KEY = "ResponseMessage";

    @Override
    public String getValue(Connection connection, Request request, Response response) {
        return response.message();
    }

    @Override
    public String getKey() {
        return KEY;
    }
}

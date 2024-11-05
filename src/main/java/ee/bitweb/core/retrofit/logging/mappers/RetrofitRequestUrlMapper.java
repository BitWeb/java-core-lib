package ee.bitweb.core.retrofit.logging.mappers;

import lombok.RequiredArgsConstructor;
import okhttp3.Request;
import okhttp3.Response;

@RequiredArgsConstructor
public class RetrofitRequestUrlMapper implements RetrofitLoggingMapper {

    public static final String KEY = "request_url";

    @Override
    public String getValue(Request request, Response response) {
        return request.url().toString();
    }

    @Override
    public String getKey() {
        return KEY;
    }
}

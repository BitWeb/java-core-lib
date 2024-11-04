package ee.bitweb.core.retrofit.logging.mappers;

import lombok.RequiredArgsConstructor;
import okhttp3.Request;
import okhttp3.Response;

@RequiredArgsConstructor
public class RetrofitResponseStatusCodeMapper implements RetrofitLoggingMapper {

    public static final String KEY = "response_code";

    @Override
    public String getValue(Request request, Response response) {
        return String.valueOf(response.code());
    }

    @Override
    public String getKey() {
        return KEY;
    }
}

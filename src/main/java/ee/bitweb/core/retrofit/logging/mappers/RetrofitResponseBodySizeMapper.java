package ee.bitweb.core.retrofit.logging.mappers;

import lombok.RequiredArgsConstructor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@RequiredArgsConstructor
public class RetrofitResponseBodySizeMapper implements RetrofitLoggingMapper {

    public static final String KEY = "response_body_size";

    @Override
    public String getValue(Request request, Response response) {
        if (response == null) {
            return "-";
        }

        ResponseBody body = response.body();

        return body != null ? String.valueOf(body.contentLength()) : "-";
    }

    @Override
    public String getKey() {
        return KEY;
    }
}

package ee.bitweb.core.retrofit.logging.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class RetrofitRequestBodySizeMapper implements RetrofitLoggingMapper {

    public static final String KEY = "request_body_size";

    @Override
    public String getValue(Request request, Response response) {
        try {
            RequestBody body = request.body();

            return body != null ? String.valueOf(body.contentLength()) : "-";
        } catch (IOException e) {
            log.error("Failed to parse request content length.", e);
            return "Parse error.";
        }
    }

    @Override
    public String getKey() {
        return KEY;
    }
}

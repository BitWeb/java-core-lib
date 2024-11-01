package ee.bitweb.core.retrofit.logging.mappers;

import lombok.RequiredArgsConstructor;
import okhttp3.Request;
import okhttp3.Response;

@RequiredArgsConstructor
public class RetrofitRequestMethodMapper implements RetrofitLoggingMapper {

    public static final String KEY = "RequestMethod";

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getValue(Request request, Response response) {
        return request.method();
    }
}

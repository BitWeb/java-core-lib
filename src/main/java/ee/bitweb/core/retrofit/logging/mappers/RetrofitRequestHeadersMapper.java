package ee.bitweb.core.retrofit.logging.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static ee.bitweb.core.retrofit.logging.mappers.RetrofitHeadersMapperHelper.*;

@Slf4j
@RequiredArgsConstructor
public class RetrofitRequestHeadersMapper implements RetrofitLoggingMapper {

    public static final String KEY = "RequestHeaders";

    private final Set<String> redactHeaders;

    @Override
    public String getValue(Request request, Response response) {
        try {
            return getRequestHeadersString(request);
        } catch (IOException e) {
            log.error("Failed to parse request headers.", e);
            return "Parse error";
        }
    }

    @Override
    public String getKey() {
        return KEY;
    }

    protected String getRequestHeadersString(Request request) throws IOException {
        Map<String, String> result = new HashMap<>();

        var requestHeaders = request.headers();
        var requestBody = request.body();

        String contentType = getRequestContentTypeValue(requestHeaders, requestBody);
        String contentLength = getRequestContentLengthValue(requestHeaders, requestBody);

        if (contentType != null) {
            RetrofitHeadersMapperHelper.addHeaderToResult(redactHeaders, result, CONTENT_TYPE, contentType);
        }

        if (contentLength != null) {
            RetrofitHeadersMapperHelper.addHeaderToResult(redactHeaders, result, CONTENT_LENGTH, contentLength);
        }

        for (int i = 0; i < requestHeaders.size(); i++) {
            var name = requestHeaders.name(i);
            if (CONTENT_TYPE.equalsIgnoreCase(name) || CONTENT_LENGTH.equalsIgnoreCase(name)) {
                continue;
            }
            addHeaderToResult(redactHeaders, result, name, requestHeaders.value(i));
        }

        return writeHeadersMapAsString(result);
    }

    protected String getRequestContentTypeValue(Headers headers, RequestBody body) {
        return RetrofitHeadersMapperHelper.getContentTypeValue(headers, body != null ? body.contentType() : null);
    }

    protected String getRequestContentLengthValue(Headers headers, RequestBody body) throws IOException {
        return RetrofitHeadersMapperHelper.getContentLengthValue(headers, body != null ? body.contentLength() : null);
    }
}

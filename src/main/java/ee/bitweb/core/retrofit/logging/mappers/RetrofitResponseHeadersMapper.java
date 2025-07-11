package ee.bitweb.core.retrofit.logging.mappers;

import okhttp3.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ee.bitweb.core.retrofit.logging.mappers.RetrofitHeadersMapperHelper.CONTENT_LENGTH;
import static ee.bitweb.core.retrofit.logging.mappers.RetrofitHeadersMapperHelper.CONTENT_TYPE;

public class RetrofitResponseHeadersMapper implements RetrofitLoggingMapper {

    public static final String KEY = "response_headers";

    private final Set<String> redactHeaders;

    public RetrofitResponseHeadersMapper(Set<String> redactHeaders) {
        this.redactHeaders = redactHeaders.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }

    @Override
    public String getValue(Request request, Response response) {
        return getResponseHeadersString(response);
    }

    @Override
    public String getKey() {
        return KEY;
    }

    protected String getResponseHeadersString(Response response) {
        if (response == null) {
            return "(response missing)";
        }

        Map<String, String> result = new HashMap<>();

        var responseHeaders = response.headers();
        var responseBody = response.body();

        String contentType = getResponseContentTypeValue(responseHeaders, responseBody);
        String contentLength = getResponseContentLengthValue(responseHeaders, responseBody);

        if (contentType != null) {
            RetrofitHeadersMapperHelper.addHeaderToResult(redactHeaders, result, CONTENT_TYPE, contentType);
        }

        if (contentLength != null) {
            RetrofitHeadersMapperHelper.addHeaderToResult(redactHeaders, result, CONTENT_LENGTH, contentLength);
        }

        for (int i = 0; i < responseHeaders.size(); i++) {
            var name = responseHeaders.name(i);
            if (CONTENT_TYPE.equalsIgnoreCase(name) || CONTENT_LENGTH.equalsIgnoreCase(name)) {
                continue;
            }
            RetrofitHeadersMapperHelper.addHeaderToResult(redactHeaders, result, name, responseHeaders.value(i));
        }

        return RetrofitHeadersMapperHelper.writeHeadersMapAsString(result);
    }

    protected String getResponseContentTypeValue(Headers headers, ResponseBody body) {
        return RetrofitHeadersMapperHelper.getContentTypeValue(headers, body != null ? body.contentType() : null);
    }

    protected String getResponseContentLengthValue(Headers headers, ResponseBody body) {
        return RetrofitHeadersMapperHelper.getContentLengthValue(headers, body != null ? body.contentLength() : null);
    }
}
